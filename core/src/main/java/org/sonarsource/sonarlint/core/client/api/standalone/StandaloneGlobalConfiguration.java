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
package org.sonarsource.sonarlint.core.client.api.standalone;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import org.sonarsource.sonarlint.core.client.api.common.AbstractGlobalConfiguration;

/**
 * To use in standalone mode please provide list of plugin URLs.
 */
@Immutable
public class StandaloneGlobalConfiguration extends AbstractGlobalConfiguration {

  private final Set<Path> pluginJarPaths;

  private StandaloneGlobalConfiguration(Builder builder) {
    super(builder);
    this.pluginJarPaths = Set.copyOf(builder.pluginJarPaths);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Set<Path> getPluginJarPaths() {
    return pluginJarPaths;
  }

  public static final class Builder extends AbstractBuilder<Builder> {
    private final List<Path> pluginJarPaths = new ArrayList<>();

    private Builder() {
    }

    public Builder addPlugins(Path... pluginJars) {
      Collections.addAll(this.pluginJarPaths, pluginJars);
      return this;
    }

    public Builder addPlugin(Path pluginJar) {
      this.pluginJarPaths.add(pluginJar);
      return this;
    }

    public StandaloneGlobalConfiguration build() {
      return new StandaloneGlobalConfiguration(this);
    }
  }

}
