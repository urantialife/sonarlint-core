/*
 * SonarLint Core - Implementation
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.sonarlint.core.container.connected.sync;

import com.google.gson.Gson;
import java.nio.file.Path;
import org.sonarqube.ws.client.WsResponse;
import org.sonarsource.sonarlint.core.container.connected.SonarLintWsClient;
import org.sonarsource.sonarlint.core.container.storage.ProtobufUtil;
import org.sonarsource.sonarlint.core.container.storage.StorageManager;
import org.sonarsource.sonarlint.core.proto.Sonarlint.ModuleList;
import org.sonarsource.sonarlint.core.proto.Sonarlint.ModuleList.Module.Builder;

public class ModuleListSync {

  private final SonarLintWsClient wsClient;

  public ModuleListSync(SonarLintWsClient wsClient) {
    this.wsClient = wsClient;
  }

  public void fetchModulesList(Path dest) {
    WsResponse response = wsClient.get("api/projects/index?format=json&subprojects=true");
    DefaultModule[] results = new Gson().fromJson(response.contentReader(), DefaultModule[].class);
    ModuleList.Builder moduleListBuilder = ModuleList.newBuilder();
    Builder moduleBuilder = ModuleList.Module.newBuilder();
    for (DefaultModule module : results) {
      moduleBuilder.clear();
      moduleListBuilder.getMutableModulesByKey().put(module.k, moduleBuilder
        .setKey(module.k)
        .setName(module.nm)
        .build());
    }
    ProtobufUtil.writeToFile(moduleListBuilder.build(), dest.resolve(StorageManager.MODULE_LIST_PB));
  }

  private static class DefaultModule {
    String k;
    String nm;
  }

}
