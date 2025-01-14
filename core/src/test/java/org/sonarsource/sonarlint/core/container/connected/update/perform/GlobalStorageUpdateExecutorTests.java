/*
 * SonarLint Core - Implementation
 * Copyright (C) 2016-2022 SonarSource SA
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
package org.sonarsource.sonarlint.core.container.connected.update.perform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonarqube.ws.Components;
import org.sonarqube.ws.Qualityprofiles;
import org.sonarqube.ws.Rules;
import org.sonarqube.ws.Settings;
import org.sonarsource.sonarlint.core.MockWebServerExtensionWithProtobuf;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.container.storage.ProjectStoragePaths;
import org.sonarsource.sonarlint.core.container.storage.ProtobufUtil;
import org.sonarsource.sonarlint.core.container.storage.ServerInfoStore;
import org.sonarsource.sonarlint.core.container.storage.ServerStorage;
import org.sonarsource.sonarlint.core.proto.Sonarlint.ServerInfos;
import org.sonarsource.sonarlint.core.proto.Sonarlint.StorageStatus;
import org.sonarsource.sonarlint.core.util.VersionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalStorageUpdateExecutorTests {

  private static final ProgressMonitor PROGRESS = new ProgressMonitor(null);

  @RegisterExtension
  static MockWebServerExtensionWithProtobuf mockServer = new MockWebServerExtensionWithProtobuf();

  private GlobalStorageUpdateExecutor globalUpdate;

  private Path destDir;

  @BeforeEach
  void setUp(@TempDir Path temp) throws IOException {

    mockServer.addStringResponse("/api/system/status", "{\"id\": \"20160308094653\",\"version\": \"7.9\",\"status\": \"UP\"}");
    mockServer.addProtobufResponse("/api/settings/values.protobuf", Settings.ValuesWsResponse.newBuilder().build());
    mockServer.addProtobufResponse("/api/components/search.protobuf?qualifiers=TRK&ps=500&p=1", Components.SearchWsResponse.newBuilder().build());
    mockServer.addProtobufResponse("/api/qualityprofiles/search.protobuf", Qualityprofiles.SearchWsResponse.newBuilder().build());
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?f=repo,name,severity,lang,htmlDesc,htmlNote,internalKey,isTemplate,templateKey,actives&statuses=BETA,DEPRECATED,READY&types=CODE_SMELL,BUG,VULNERABILITY&severities=INFO&languages=&p=1&ps=500",
      Rules.SearchResponse.newBuilder().build());
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?f=repo,name,severity,lang,htmlDesc,htmlNote,internalKey,isTemplate,templateKey,actives&statuses=BETA,DEPRECATED,READY&types=CODE_SMELL,BUG,VULNERABILITY&severities=MINOR&languages=&p=1&ps=500",
      Rules.SearchResponse.newBuilder().build());
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?f=repo,name,severity,lang,htmlDesc,htmlNote,internalKey,isTemplate,templateKey,actives&statuses=BETA,DEPRECATED,READY&types=CODE_SMELL,BUG,VULNERABILITY&severities=MAJOR&languages=&p=1&ps=500",
      Rules.SearchResponse.newBuilder().build());
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?f=repo,name,severity,lang,htmlDesc,htmlNote,internalKey,isTemplate,templateKey,actives&statuses=BETA,DEPRECATED,READY&types=CODE_SMELL,BUG,VULNERABILITY&severities=CRITICAL&languages=&p=1&ps=500",
      Rules.SearchResponse.newBuilder().build());
    mockServer.addProtobufResponse(
      "/api/rules/search.protobuf?f=repo,name,severity,lang,htmlDesc,htmlNote,internalKey,isTemplate,templateKey,actives&statuses=BETA,DEPRECATED,READY&types=CODE_SMELL,BUG,VULNERABILITY&severities=BLOCKER&languages=&p=1&ps=500",
      Rules.SearchResponse.newBuilder().build());

    destDir = temp.resolve("storage/6964/global");

    globalUpdate = new GlobalStorageUpdateExecutor(new ServerStorage(destDir));
  }

  @Test
  void testUpdate() {
    globalUpdate.update(mockServer.serverApiHelper(), PROGRESS);

    var updateStatus = ProtobufUtil.readFile(destDir.resolve(ProjectStoragePaths.STORAGE_STATUS_PB), StorageStatus.parser());
    assertThat(updateStatus.getSonarlintCoreVersion()).isEqualTo(VersionUtils.getLibraryVersion());
    assertThat(updateStatus.getUpdateTimestamp()).isNotZero();

    var serverInfos = ProtobufUtil.readFile(destDir.resolve(ServerInfoStore.SERVER_INFO_PB), ServerInfos.parser());
    assertThat(serverInfos.getId()).isEqualTo("20160308094653");
    assertThat(serverInfos.getVersion()).isEqualTo("7.9");
  }

  @Test
  void dontCopyOnError() throws IOException {
    Files.createDirectories(destDir);
    Files.createFile(destDir.resolve("test"));
    var mockProgress = mock(ProgressMonitor.class);
    when(mockProgress.subProgress(anyFloat(), anyFloat(), anyString())).thenReturn(mockProgress);
    doThrow(new IllegalStateException("Boom")).when(mockProgress).executeNonCancelableSection(any());
    var throwable = catchThrowable(() -> globalUpdate.update(mockServer.serverApiHelper(), mockProgress));

    assertThat(throwable).isInstanceOf(IllegalStateException.class);
    // dest left untouched
    assertThat(Files.exists(destDir.resolve("test"))).isTrue();
  }
}
