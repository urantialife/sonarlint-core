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
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.sonarlint.core.client.api.common.HttpClient;
import org.sonarsource.sonarlint.core.client.api.util.SonarLintUtils;

public class TelemetryClient {

  private static final Logger LOG = Loggers.get(TelemetryClient.class);
  static final String JSON_CONTENT_TYPE = "application/json";
  static final URL TELEMETRY_ENDPOINT;

  static {
    try {
      TELEMETRY_ENDPOINT = new URL("https://chestnutsl.sonarsource.com/telemetry");
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL", e);
    }
  }

  private final HttpClient client;
  private final String product;
  private final String version;
  private final String ideVersion;

  TelemetryClient(String product, String version, String ideVersion, HttpClient client) {
    this.product = product;
    this.version = version;
    this.ideVersion = ideVersion;
    this.client = client;
  }

  void upload(TelemetryData data, boolean usesConnectedMode, boolean usesSonarCloud) {
    try {
      TelemetryPayload payload = createPayload(data, usesConnectedMode, usesSonarCloud);
      client.post(TELEMETRY_ENDPOINT, JSON_CONTENT_TYPE, payload.toJson());
    } catch (Exception e) {
      if (SonarLintUtils.isInternalDebugEnabled()) {
        LOG.error("Failed to upload telemetry data", e);
      }
    }
  }

  void optOut(TelemetryData data, boolean usesConnectedMode, boolean usesSonarCloud) {
    try {
      TelemetryPayload payload = createPayload(data, usesConnectedMode, usesSonarCloud);
      client.delete(TELEMETRY_ENDPOINT, JSON_CONTENT_TYPE, payload.toJson());
    } catch (Exception e) {
      if (SonarLintUtils.isInternalDebugEnabled()) {
        LOG.error("Failed to upload telemetry opt-out", e);
      }
    }
  }

  private TelemetryPayload createPayload(TelemetryData data, boolean usesConnectedMode, boolean usesSonarCloud) {
    OffsetDateTime systemTime = OffsetDateTime.now();
    long daysSinceInstallation = data.installTime().until(systemTime, ChronoUnit.DAYS);
    TelemetryAnalyzerPerformancePayload[] analyzers = TelemetryUtils.toPayload(data.analyzers());
    return new TelemetryPayload(daysSinceInstallation, data.numUseDays(), product, version, ideVersion,
      usesConnectedMode, usesSonarCloud, systemTime, data.installTime(), analyzers);
  }

}
