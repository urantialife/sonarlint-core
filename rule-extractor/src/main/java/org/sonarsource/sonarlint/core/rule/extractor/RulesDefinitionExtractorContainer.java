/*
 * SonarLint Core - Rule Extractor
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
package org.sonarsource.sonarlint.core.rule.extractor;

import org.sonar.api.SonarQubeVersion;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.AnnotationUtils;
import org.sonarsource.api.sonarlint.SonarLintSide;
import org.sonarsource.sonarlint.core.plugin.commons.ApiVersions;
import org.sonarsource.sonarlint.core.plugin.commons.ExtensionInstaller;
import org.sonarsource.sonarlint.core.plugin.commons.ExtensionUtils;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository;
import org.sonarsource.sonarlint.core.plugin.commons.pico.ComponentContainer;
import org.sonarsource.sonarlint.core.plugin.commons.sonarapi.SonarLintRuntimeImpl;

public class RulesDefinitionExtractorContainer extends ComponentContainer {

  private final PluginInstancesRepository pluginInstancesRepository;
  private Context context;

  public RulesDefinitionExtractorContainer(PluginInstancesRepository pluginInstancesRepository) {
    this.pluginInstancesRepository = pluginInstancesRepository;
  }

  @Override
  protected void doBeforeStart() {
    var sonarPluginApiVersion = ApiVersions.loadSonarPluginApiVersion();
    var sonarlintPluginApiVersion = ApiVersions.loadSonarLintPluginApiVersion();

    var sonarLintRuntime = new SonarLintRuntimeImpl(sonarPluginApiVersion, sonarlintPluginApiVersion, -1);

    var config = new EmptyConfiguration();

    var extensionInstaller = new ExtensionInstaller(sonarLintRuntime, config);
    extensionInstaller.install(this, pluginInstancesRepository.getPluginInstancesByKeys(), (key, ext) -> {
      if (ExtensionUtils.isType(ext, Sensor.class)) {
        // Optimization, and allows to run with the Xoo plugin
        return false;
      }
      var annotation = AnnotationUtils.getAnnotation(ext, SonarLintSide.class);
      if (annotation != null) {
        var lifespan = annotation.lifespan();
        return SonarLintSide.SINGLE_ANALYSIS.equals(lifespan);
      }
      return false;
    });

    add(
      config,
      sonarLintRuntime,
      new SonarQubeVersion(sonarPluginApiVersion),
      RulesDefinitionXmlLoader.class,
      RuleDefinitionsLoader.class,
      NoopTempFolder.class,
      EmptySettings.class);
  }

  @Override
  protected void doAfterStart() {
    this.context = getComponentByType(RuleDefinitionsLoader.class).getContext();
  }

  public Context getContext() {
    return context;
  }

}
