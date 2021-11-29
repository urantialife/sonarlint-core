/*
 * SonarLint Core - Implementation
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
package org.sonarsource.sonarlint.core.container.model;

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.plugin.common.SkipReason;

public class DefaultLoadedAnalyzer implements PluginDetails {
  private final String key;
  private final String name;
  private final String version;
  private final SkipReason skipReason;

  public DefaultLoadedAnalyzer(String key, String name, @Nullable String version, @Nullable SkipReason skipReason) {
    this.key = key;
    this.name = name;
    this.version = version;
    this.skipReason = skipReason;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String version() {
    return version;
  }

  @Override
  public Optional<SkipReason> skipReason() {
    return Optional.ofNullable(skipReason);
  }
}
