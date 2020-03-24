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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;

public class ServerConfiguration {

  private final String url;
  private final String organizationKey;
  private final HttpClient client;

  private ServerConfiguration(Builder builder) {
    this.url = removeTrailingSlash(builder.url);
    this.organizationKey = builder.organizationKey;
    this.client = builder.client;
  }

  private static String removeTrailingSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  public String getUrl() {
    return url;
  }

  @CheckForNull
  public String getOrganizationKey() {
    return organizationKey;
  }

  public HttpClient httpClient() {
    return client;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String url;
    private String organizationKey;
    private HttpClient client;

    private Builder() {
    }

    public Builder httpClient(HttpClient client) {
      this.client = client;
      return this;
    }

    /**
     * Mandatory HTTP server URL, eg "http://localhost:9000"
     */
    public Builder url(String url) {
      this.url = url;
      return this;
    }

    /**
     * Optional organization
     * @param organizationKey key
     */
    public Builder organizationKey(@Nullable String organizationKey) {
      this.organizationKey = organizationKey;
      return this;
    }

    public ServerConfiguration build() {
      if (url == null) {
        throw new UnsupportedOperationException("Server URL is mandatory");
      }
      if (client == null) {
        throw new UnsupportedOperationException("Client is mandatory");
      }
      return new ServerConfiguration(this);
    }

  }

}
