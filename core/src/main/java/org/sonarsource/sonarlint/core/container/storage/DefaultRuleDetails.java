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
package org.sonarsource.sonarlint.core.container.storage;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedRuleDetails;
import org.sonarsource.sonarlint.core.commons.Language;

@Immutable
public class DefaultRuleDetails implements ConnectedRuleDetails {

  private final String key;
  private final Language language;
  private final String name;
  private final String htmlDescription;
  private final String severity;
  private final String type;
  private final String extendedDescription;

  public DefaultRuleDetails(String key, String name, @Nullable String htmlDescription, String severity, @Nullable String type, Language language, String extendedDescription) {
    this.key = key;
    this.name = name;
    this.htmlDescription = htmlDescription;
    this.severity = severity;
    this.type = type;
    this.language = language;
    this.extendedDescription = extendedDescription;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getHtmlDescription() {
    return htmlDescription;
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
    return type;
  }

  @Override
  public String getExtendedDescription() {
    return extendedDescription;
  }

}
