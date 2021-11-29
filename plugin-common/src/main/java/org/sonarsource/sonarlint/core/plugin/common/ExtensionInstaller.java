/*
 * SonarLint Core - Plugin Common
 * Copyright (C) 2016-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonarsource.sonarlint.core.plugin.common;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import org.sonar.api.Plugin;
import org.sonar.api.config.Configuration;
import org.sonarsource.sonarlint.core.plugin.common.pico.ComponentContainer;
import org.sonarsource.sonarlint.core.plugin.common.sonarapi.PluginContextImpl;
import org.sonarsource.sonarlint.plugin.api.SonarLintRuntime;

public class ExtensionInstaller {

  private final SonarLintRuntime sonarRuntime;
  private final Configuration bootConfiguration;

  public ExtensionInstaller(SonarLintRuntime sonarRuntime, Configuration bootConfiguration) {
    this.sonarRuntime = sonarRuntime;
    this.bootConfiguration = bootConfiguration;
  }

  public ExtensionInstaller install(ComponentContainer container, Map<String, Plugin> pluginInstancesByKey, BiPredicate<String, Object> extensionFilter) {
    for (Entry<String, Plugin> pluginInstanceEntry : pluginInstancesByKey.entrySet()) {
      Plugin plugin = pluginInstanceEntry.getValue();
      Plugin.Context context = new PluginContextImpl.Builder()
        .setSonarRuntime(sonarRuntime)
        .setBootConfiguration(bootConfiguration)
        .build();
      plugin.define(context);
      loadExtensions(container, pluginInstanceEntry.getKey(), context, extensionFilter);
    }
    return this;
  }

  private void loadExtensions(ComponentContainer container, String pluginKey, Plugin.Context context, BiPredicate<String, Object> extensionFilter) {
    for (Object extension : context.getExtensions()) {
      // filter out non officially supported Sensors
      if (extensionFilter.test(pluginKey, extension)) {
        container.addExtension(pluginKey, extension);
      }
    }
  }

}