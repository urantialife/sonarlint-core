/*
 * SonarLint Core - Client API
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
package org.sonarsource.sonarlint.core.client.api.connected;

import org.junit.Test;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class ServerConfigurationTest {

  @Test
  public void builder_url_mandatory() {
    try {
      ServerConfiguration.builder().build();
      fail("Expected exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Server URL is mandatory");
    }
  }

  @Test
  public void builder_http_client_mandatory() {
    try {
      ServerConfiguration.builder()
        .url("http://foo")
        .build();
      fail("Expected exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Client is mandatory");
    }
  }

  @Test
  public void minimal_builder() {
    HttpClient httpClient = mock(HttpClient.class);
    ServerConfiguration config = ServerConfiguration.builder()
      .url("http://foo")
      .httpClient(httpClient)
      .build();
    assertThat(config.getUrl()).isEqualTo("http://foo");
    assertThat(config.httpClient()).isSameAs(httpClient);
  }

  @Test
  public void max_builder() {
    HttpClient httpClient = mock(HttpClient.class);
    ServerConfiguration config = ServerConfiguration.builder()
      .url("http://foo")
      .organizationKey("org")
      .httpClient(httpClient)
      .build();
    assertThat(config.getUrl()).isEqualTo("http://foo");
    assertThat(config.getOrganizationKey()).isEqualTo("org");
    assertThat(config.httpClient()).isSameAs(httpClient);
  }
}
