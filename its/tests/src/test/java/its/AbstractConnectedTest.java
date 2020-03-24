/*
 * SonarLint Core - ITs - Tests
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
package its;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sonar.orchestrator.Orchestrator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;
import org.sonarsource.sonarlint.core.client.api.common.analysis.Issue;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedAnalysisConfiguration;

public class AbstractConnectedTest {
  private static final String USER_AGENT = "SonarLint ITs";
  private static final String SONARLINT_USER = "sonarlint";
  private static final String SONARLINT_PWD = "sonarlintpwd";

  protected static final HttpClient client = new JdkHttpClientImplementation(true);

  @ClassRule
  public static TemporaryFolder t = new TemporaryFolder();

  protected static final class JdkHttpClientImplementation implements HttpClient {

    private final class GetResponseImplementation implements GetResponse {
      private final HttpURLConnection conn;
      private final URL endpoint;

      private GetResponseImplementation(HttpURLConnection conn, URL endpoint) {
        this.conn = conn;
        this.endpoint = endpoint;
      }

      @Override
      public URL url() {
        return endpoint;
      }

      @Override
      public InputStream contentStream() {
        try {
          return conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
            ? conn.getInputStream()
            : conn.getErrorStream();
        } catch (IOException e) {
          throw new IllegalStateException("Error", e);
        }
      }

      @Override
      public String content() {
        try (InputStream is = contentStream()) {
          return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw new IllegalStateException("Error", e);
        }
      }

      @Override
      public int code() {
        try {
          return conn.getResponseCode();
        } catch (IOException e) {
          throw new IllegalStateException("Error", e);
        }
      }

      @Override
      public void close() {
      }
    }

    private boolean passCredentials;

    public JdkHttpClientImplementation(boolean passCredentials) {
      this.passCredentials = passCredentials;
    }

    @Override
    public void post(URL endpoint, String contentType, String body) {
      try {
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();

        conn.setRequestMethod("POST");
        setUserAgentAndCredentials(conn);
        conn.setDoOutput(true);
        try (OutputStream outputStream = conn.getOutputStream()) {
          IOUtils.write(body.getBytes(StandardCharsets.UTF_8), outputStream);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
          throw new IllegalStateException("Error sending POST request");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Unexpected exception", e);
      }
    }

    private void setUserAgentAndCredentials(HttpURLConnection conn) {
      conn.setRequestProperty("User-Agent", USER_AGENT);

      if (passCredentials) {
        String auth = SONARLINT_USER + ":" + SONARLINT_PWD;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        conn.setRequestProperty("Authorization", authHeaderValue);
      }
    }

    @Override
    public GetResponse get(URL endpoint) {
      try {
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("GET");
        setUserAgentAndCredentials(conn);
        boolean redirect = false;

        // normally, 3xx is redirect
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
          || status == HttpURLConnection.HTTP_MOVED_PERM
          || status == HttpURLConnection.HTTP_SEE_OTHER) {
          redirect = true;
        }
        if (redirect) {

          // get redirect url from "location" header field
          String newUrl = conn.getHeaderField("Location");

          // get the cookie if need, for login
          String cookies = conn.getHeaderField("Set-Cookie");

          // open the new connnection again
          conn = (HttpURLConnection) new URL(newUrl).openConnection();
          conn.setRequestProperty("Cookie", cookies);
          setUserAgentAndCredentials(conn);
        }
        return new GetResponseImplementation(conn, endpoint);
      } catch (IOException e) {
        throw new IllegalStateException("Unexpected exception", e);
      }
    }

    @Override
    public void delete(URL endpoint, String contentType, String body) {
      try {
        HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();

        conn.setRequestMethod("DELETE");
        setUserAgentAndCredentials(conn);
        conn.setDoOutput(true);
        try (OutputStream outputStream = conn.getOutputStream()) {
          IOUtils.write(body.getBytes(StandardCharsets.UTF_8), outputStream);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
          throw new IllegalStateException("Error sending DELETE request");
        }
      } catch (IOException e) {
        throw new IllegalStateException("Unexpected exception", e);
      }
    }
  }

  protected static class SaveIssueListener implements IssueListener {
    List<Issue> issues = new LinkedList<>();

    @Override
    public void handle(Issue issue) {
      issues.add(issue);
    }

    public List<Issue> getIssues() {
      return issues;
    }

    public void clear() {
      issues.clear();
    }
  }

  protected ConnectedAnalysisConfiguration createAnalysisConfiguration(String projectKey, String projectDir, String filePath, String... properties) throws IOException {
    final Path baseDir = Paths.get("projects/" + projectDir).toAbsolutePath();
    final Path path = baseDir.resolve(filePath);
    return ConnectedAnalysisConfiguration.builder()
      .setProjectKey(projectKey)
      .setBaseDir(new File("projects/" + projectDir).toPath().toAbsolutePath())
      .addInputFile(new TestClientInputFile(baseDir, path, false, StandardCharsets.UTF_8))
      .putAllExtraProperties(toMap(properties))
      .build();
  }

  protected ConnectedAnalysisConfiguration createAnalysisConfiguration(String projectKey, String absoluteFilePath) throws IOException {
    final Path path = Paths.get(absoluteFilePath).toAbsolutePath();
    return ConnectedAnalysisConfiguration.builder()
      .setProjectKey(projectKey)
      .setBaseDir(path.getParent())
      .addInputFile(new TestClientInputFile(path.getParent(), path, false, StandardCharsets.UTF_8))
      .build();
  }

  static Map<String, String> toMap(String[] keyValues) {
    Preconditions.checkArgument(keyValues.length % 2 == 0, "Must be an even number of key/values");
    Map<String, String> map = Maps.newHashMap();
    int index = 0;
    while (index < keyValues.length) {
      String key = keyValues[index++];
      String value = keyValues[index++];
      map.put(key, value);
    }
    return map;
  }

  protected static WsClient newAdminWsClient(Orchestrator orchestrator) {
    com.sonar.orchestrator.container.Server server = orchestrator.getServer();
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(server.getUrl())
      .credentials(com.sonar.orchestrator.container.Server.ADMIN_LOGIN, com.sonar.orchestrator.container.Server.ADMIN_PASSWORD)
      .build());
  }

  protected static void createSonarLintUser(WsClient adminWsClient) {
    adminWsClient.users().create(org.sonarqube.ws.client.user.CreateRequest.builder()
      .setLogin(SONARLINT_USER)
      .setPassword(SONARLINT_PWD)
      .setName("SonarLint")
      .build());
  }
}
