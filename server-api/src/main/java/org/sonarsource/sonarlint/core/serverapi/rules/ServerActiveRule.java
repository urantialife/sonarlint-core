/*
 * SonarLint Server API
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
package org.sonarsource.sonarlint.core.serverapi.rules;

import java.util.Map;
import javax.annotation.Nullable;

public class ServerActiveRule {
  private final String ruleKey;
  private final String severity;
  private final Map<String, String> params;
  private final String templateKey;

  public ServerActiveRule(String ruleKey, String severity, Map<String, String> params, @Nullable String templateKey) {
    this.ruleKey = ruleKey;
    this.severity = severity;
    this.params = params;
    this.templateKey = templateKey;
  }

  public String getSeverity() {
    return severity;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public String getRuleKey() {
    return ruleKey;
  }

  public String getTemplateKey() {
    return templateKey;
  }
}
