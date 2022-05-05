/*
 * SonarLint Commons
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
package org.sonarsource.sonarlint.core.commons.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.sonarsource.sonarlint.core.commons.http.HttpClient;
import org.sonarsource.sonarlint.core.commons.http.HttpConnectionListener;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;

public class MockWebServerExtension implements BeforeEachCallback, AfterEachCallback {

  private static final java.net.http.HttpClient SHARED_CLIENT = java.net.http.HttpClient.newBuilder().build();

  private MockWebServer server;
  protected final Map<String, MockResponse> responsesByPath = new HashMap<>();

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    server = new MockWebServer();
    responsesByPath.clear();
    final Dispatcher dispatcher = new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        if (responsesByPath.containsKey(request.getPath())) {
          return responsesByPath.get(request.getPath());
        }
        return new MockResponse().setResponseCode(404);
      }
    };
    server.setDispatcher(dispatcher);
    server.start();
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    server.shutdown();
  }

  public void addStringResponse(String path, String body) {
    responsesByPath.put(path, new MockResponse().setBody(body));
  }

  public void removeResponse(String path) {
    responsesByPath.remove(path);
  }

  public void addResponse(String path, MockResponse response) {
    responsesByPath.put(path, response);
  }

  public int getRequestCount() {
    return server.getRequestCount();
  }

  public RecordedRequest takeRequest() {
    try {
      return server.takeRequest();
    } catch (InterruptedException e) {
      fail(e);
      return null; // appeasing the compiler: this line will never be executed.
    }
  }

  public String url(String path) {
    return server.url(path).toString();
  }

  public void addResponseFromResource(String path, String responseResourcePath) {
    try (var b = new Buffer()) {
      responsesByPath.put(path, new MockResponse().setBody(b.readFrom(requireNonNull(MockWebServerExtension.class.getResourceAsStream(responseResourcePath)))));
    } catch (IOException e) {
      fail(e);
    }
  }

  public static HttpClient httpClient() {
    return new HttpClient() {

      @Override
      public Response post(String url, String contentType, String bodyContent) {
        var request = HttpRequest.newBuilder().uri(URI.create(url))
          .headers("Content-Type", contentType)
          .POST(HttpRequest.BodyPublishers.ofString(bodyContent)).build();
        return executeRequest(request);
      }

      @Override
      public Response get(String url) {
        var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return executeRequest(request);
      }

      @Override
      public CompletableFuture<Response> getAsync(String url) {
        var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        return executeRequestAsync(request);
      }

      @Override
      public AsyncRequest getEventStream(String url, HttpConnectionListener connectionListener, Consumer<String> messageConsumer) {
        var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        var responseFuture = SHARED_CLIENT.sendAsync(request, BodyHandlers.ofInputStream());
        var wrappedFuture = responseFuture.whenComplete((response, ex) -> {
          if (ex != null) {
            connectionListener.onError(null);
          } else {
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
              connectionListener.onConnected();
              // simplified reading, for tests we assume the event comes full, never chunked
              messageConsumer.accept(wrap(response).bodyAsString());
            } else {
              connectionListener.onError(response.statusCode());
            }
          }
        });
        return new HttpAsyncRequest(wrappedFuture);
      }

      @Override
      public Response delete(String url, String contentType, String bodyContent) {
        var request = HttpRequest.newBuilder().uri(URI.create(url))
          .headers("Content-Type", contentType)
          .method("DELETE", HttpRequest.BodyPublishers.ofString(bodyContent)).build();
        return executeRequest(request);
      }

      private Response executeRequest(HttpRequest request) {
        try {
          return wrap(SHARED_CLIENT.send(request, BodyHandlers.ofInputStream()));
        } catch (Exception e) {
          throw new IllegalStateException("Unable to execute request: " + e.getMessage(), e);
        }
      }

      private CompletableFuture<Response> executeRequestAsync(HttpRequest request) {
        var call = SHARED_CLIENT.sendAsync(request, BodyHandlers.ofInputStream());
        return call.thenApply(r -> wrap(r));
      }

      private Response wrap(HttpResponse<InputStream> wrapped) {
        return new Response() {

          @Override
          public String url() {
            return wrapped.request().uri().toString();
          }

          @Override
          public int code() {
            return wrapped.statusCode();
          }

          @Override
          public void close() {
            // Nothing
          }

          @Override
          public String bodyAsString() {
            try (var body = wrapped.body()) {
              return new String(body.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
              throw new IllegalStateException("Unable to read response body: " + e.getMessage(), e);
            }
          }

          @Override
          public InputStream bodyAsStream() {
            return wrapped.body();
          }

          @Override
          public String toString() {
            return wrapped.toString();
          }
        };
      }

    };
  }

  public static class HttpAsyncRequest implements HttpClient.AsyncRequest {
    private final CompletableFuture<HttpResponse<InputStream>> response;

    private HttpAsyncRequest(CompletableFuture<HttpResponse<InputStream>> response) {
      this.response = response;
    }

    @Override
    public void cancel() {
      response.cancel(true);
    }

  }

}
