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
package org.sonarsource.sonarlint.core.telemetry;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TelemetryClientTest {
  private TelemetryClient client;
  private HttpClient http;

  @Rule
  public final EnvironmentVariables env = new EnvironmentVariables();

  @Rule
  public LogTester logTester = new LogTester();

  @Before
  public void setUp() {
    http = mock(HttpClient.class);
    client = new TelemetryClient("product", "version", "ideversion", http);
  }

  @AfterClass
  public static void after() {
    // to avoid conflicts with SonarLintLogging
    new LogTester().setLevel(LoggerLevel.TRACE);
  }

  @Test
  public void opt_out() throws MalformedURLException {
    client.optOut(new TelemetryData(), true, true);
    verify(http).delete(eq(TelemetryClient.TELEMETRY_ENDPOINT), eq(TelemetryClient.JSON_CONTENT_TYPE), matches(
      "\\{\"days_since_installation\":0,\"days_of_use\":0,\"sonarlint_version\":\"version\",\"sonarlint_product\":\"product\",\"ide_version\":\"ideversion\",\"connected_mode_used\":true,\"connected_mode_sonarcloud\":true,"
        + "\"system_time\":\"(.*)\",\"install_time\":\"(.*)\",\"analyses\":\\[\\]\\}"));
  }

  @Test
  public void upload() {
    client.upload(new TelemetryData(), true, true);
    verify(http).post(eq(TelemetryClient.TELEMETRY_ENDPOINT), eq(TelemetryClient.JSON_CONTENT_TYPE), matches(
      "\\{\"days_since_installation\":0,\"days_of_use\":0,\"sonarlint_version\":\"version\",\"sonarlint_product\":\"product\",\"ide_version\":\"ideversion\",\"connected_mode_used\":true,\"connected_mode_sonarcloud\":true,"
        + "\"system_time\":\"(.*)\",\"install_time\":\"(.*)\",\"analyses\":\\[\\]\\}"));
  }

  @Test
  public void should_not_crash_when_cannot_upload() {
    doThrow(new RuntimeException()).when(http).post(any(URL.class), anyString(), anyString());
    client.upload(new TelemetryData(), true, true);
  }

  @Test
  public void should_not_crash_when_cannot_opt_out() {
    doThrow(new RuntimeException()).when(http).delete(any(URL.class), anyString(), anyString());
    client.optOut(new TelemetryData(), true, true);
  }

  @Test
  public void failed_upload_should_log_if_debug() {
    env.set("SONARLINT_INTERNAL_DEBUG", "true");
    doThrow(new IllegalStateException("msg")).when(http).post(any(URL.class), anyString(), anyString());
    client.upload(new TelemetryData(), true, true);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Failed to upload telemetry data");
  }

  @Test
  public void failed_optout_should_log_if_debug() {
    env.set("SONARLINT_INTERNAL_DEBUG", "true");
    doThrow(new IllegalStateException("msg")).when(http).delete(any(URL.class), anyString(), anyString());
    client.optOut(new TelemetryData(), true, true);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("Failed to upload telemetry opt-out");
  }
}
