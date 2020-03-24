/*
 * SonarLint Core - Implementation
 * Copyright (C) 2009-2020 SonarSource SA
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
package org.sonarsource.sonarlint.core;

import com.google.protobuf.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;
import org.sonarsource.sonarlint.core.container.connected.SonarLintWsClient;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WsClientTestUtils {

  public static SonarLintWsClient createMockWithResponse(String url, String response) {
    SonarLintWsClient wsClient = createMock();
    return addResponse(wsClient, url, response);
  }

  public static SonarLintWsClient createMock() {
    SonarLintWsClient wsClient = mock(SonarLintWsClient.class);
    return wsClient;
  }

  public static SonarLintWsClient addResponse(SonarLintWsClient wsClient, String url, String response) {
    HttpClient.GetResponse wsResponse = mock(HttpClient.GetResponse.class);
    when(wsClient.get(url)).thenReturn(wsResponse);
    when(wsClient.rawGet(url)).thenReturn(wsResponse);
    when(wsResponse.content()).thenReturn(response).thenThrow(new IllegalStateException("Should not call content() twice"));
    when(wsResponse.contentStream()).thenReturn(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
    when(wsResponse.code()).thenReturn(200);
    when(wsResponse.isSuccessful()).thenCallRealMethod();
    return wsClient;
  }

  public static SonarLintWsClient addResponse(SonarLintWsClient wsClient, String url, InputStream inputStream) {
    HttpClient.GetResponse wsResponse = mock(HttpClient.GetResponse.class);
    when(wsClient.get(url)).thenReturn(wsResponse);
    when(wsClient.rawGet(url)).thenReturn(wsResponse);
    when(wsResponse.code()).thenReturn(200);
    when(wsResponse.isSuccessful()).thenCallRealMethod();
    when(wsResponse.contentStream()).thenReturn(inputStream);
    return wsClient;
  }

  public static SonarLintWsClient addResponse(SonarLintWsClient wsClient, String url, Message m) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      m.writeTo(bos);
      return addResponse(wsClient, url, new ByteArrayInputStream(bos.toByteArray()));
    }
  }

  public static SonarLintWsClient addFailedResponse(SonarLintWsClient wsClient, String url, int errorCode, @Nullable String errorMsg) {
    HttpClient.GetResponse wsResponse = mock(HttpClient.GetResponse.class);
    IllegalStateException ex = new IllegalStateException(
      "Error " + errorCode + " on " + url + (errorMsg != null ? (": " + errorMsg) : ""));

    when(wsClient.get(url)).thenThrow(ex);
    when(wsClient.rawGet(url)).thenReturn(wsResponse);
    when(wsResponse.content()).thenReturn(errorMsg).thenThrow(new IllegalStateException("Should not call content() twice"));
    when(wsResponse.code()).thenReturn(errorCode);
    when(wsResponse.isSuccessful()).thenCallRealMethod();
    return wsClient;
  }

  public static SonarLintWsClient createMockWithStreamResponse(String url, String resourcePath) {
    SonarLintWsClient wsClient = createMock();
    return addStreamResponse(wsClient, url, resourcePath);
  }

  public static SonarLintWsClient addStreamResponse(SonarLintWsClient wsClient, String url, String resourcePath) {
    HttpClient.GetResponse wsResponse = mock(HttpClient.GetResponse.class);
    when(wsClient.get(url)).thenReturn(wsResponse);
    when(wsClient.rawGet(url)).thenReturn(wsResponse);
    when(wsResponse.contentStream()).thenReturn(requireNonNull(WsClientTestUtils.class.getResourceAsStream(resourcePath)));
    when(wsResponse.code()).thenReturn(200);
    when(wsResponse.isSuccessful()).thenCallRealMethod();
    return wsClient;
  }

}
