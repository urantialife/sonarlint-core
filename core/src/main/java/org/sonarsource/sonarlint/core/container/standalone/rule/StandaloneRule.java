/*
 * SonarLint Core - Implementation
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
package org.sonarsource.sonarlint.core.container.standalone.rule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleType;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleParam;
import org.sonarsource.sonarlint.core.commons.Language;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleDefinition;
import org.sonarsource.sonarlint.core.rule.extractor.SonarLintRuleParamDefinition;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Immutable
public class StandaloneRule implements StandaloneRuleDetails {

  private final RuleKey key;
  private final String name;
  private final String severity;
  private final RuleType type;
  private final String description;
  private final Map<String, DefaultStandaloneRuleParam> params;
  private final boolean isActiveByDefault;
  private final Language language;
  private final String[] tags;
  private final Set<RuleKey> deprecatedKeys;

  public StandaloneRule(SonarLintRuleDefinition ruleFromDefinition) {
    var sonarApiRuleKey = RuleKey.parse(ruleFromDefinition.getKey());
    this.key = sonarApiRuleKey;
    this.name = ruleFromDefinition.getName();
    this.severity = ruleFromDefinition.getSeverity();
    this.type = RuleType.valueOf(ruleFromDefinition.getType());
    this.description = ruleFromDefinition.getHtmlDescription();
    this.isActiveByDefault = ruleFromDefinition.isActiveByDefault();
    this.language = ruleFromDefinition.getLanguage();
    this.tags = ruleFromDefinition.getTags();
    this.deprecatedKeys = ruleFromDefinition.getDeprecatedKeys().stream().map(RuleKey::parse).collect(toSet());

    Map<String, DefaultStandaloneRuleParam> builder = new HashMap<>();
    for (SonarLintRuleParamDefinition param : ruleFromDefinition.getParams().values()) {
      builder.put(param.key(), new DefaultStandaloneRuleParam(param));
    }
    params = Collections.unmodifiableMap(builder);
  }

  @Override
  public Collection<StandaloneRuleParam> paramDetails() {
    return params.values().stream().map(StandaloneRuleParam.class::cast).collect(toList());
  }

  @Override
  public boolean isActiveByDefault() {
    return isActiveByDefault;
  }

  @Override
  public String getKey() {
    return key.toString();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getHtmlDescription() {
    return description;
  }

  @Override
  public Language getLanguage() {
    return language;
  }

  @Override
  public String getSeverity() {
    return severity;
  }

  @Override
  public String getType() {
    return type.name();
  }

  @Override
  public String[] getTags() {
    return tags;
  }

  public Set<RuleKey> getDeprecatedKeys() {
    return deprecatedKeys;
  }
}
