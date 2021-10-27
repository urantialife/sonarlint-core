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
package org.sonarsource.sonarlint.core.container.module;

import java.util.Map;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.RuleKey;

public class ActiveRuleAdapter implements ActiveRule {

  private final org.sonarsource.sonarlint.core.ActiveRule activeRule;

  ActiveRuleAdapter(org.sonarsource.sonarlint.core.ActiveRule activeRule) {
    this.activeRule = activeRule;
  }

  @Override
  public RuleKey ruleKey() {
    return RuleKey.of(activeRule.getRuleKey().repository(), activeRule.getRuleKey().rule());
  }

  @Override
  public String severity() {
    return activeRule.getSeverity();
  }

  public String type() {
    return activeRule.getType();
  }

  @Override
  public String language() {
    return activeRule.getLanguageKey();
  }

  @Override
  public String param(String key) {
    return params().get(key);
  }

  @Override
  public Map<String, String> params() {
    return activeRule.getParams();
  }

  @Override
  public String internalKey() {
    return activeRule.getInternalKey();
  }

  @Override
  public String templateRuleKey() {
    return activeRule.getTemplateRuleKey();
  }

  @Override
  public String qpKey() {
    throw new UnsupportedOperationException("qpKey not supported in SonarLint");
  }

  public String getRuleName() {
    return activeRule.getRuleName();
  }

}
