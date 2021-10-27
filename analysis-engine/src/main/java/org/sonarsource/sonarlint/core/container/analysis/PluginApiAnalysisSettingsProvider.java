/*
 * SonarLint Core - Analysis Engine
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
package org.sonarsource.sonarlint.core.container.analysis;

import java.util.HashMap;
import java.util.Map;
import org.picocontainer.injectors.ProviderAdapter;
import org.sonar.api.config.PropertyDefinitions;
import org.sonarsource.sonarlint.core.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.GlobalAnalysisConfiguration;
import org.sonarsource.sonarlint.core.container.global.MapSettings;

public class PluginApiAnalysisSettingsProvider extends ProviderAdapter {

  private MapSettings cachedValue;

  public MapSettings provide(GlobalAnalysisConfiguration globalConfig, AnalysisConfiguration analysisConfig, PropertyDefinitions propertyDefinitions) {
    if (cachedValue == null) {
      Map<String, String> props = new HashMap<>();
      props.putAll(globalConfig.getEffectiveConfig());
      props.putAll(analysisConfig.extraProperties());
      this.cachedValue = new MapSettings(propertyDefinitions, props);
    }
    return cachedValue;
  }

}
