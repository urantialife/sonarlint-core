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
package org.sonarsource.sonarlint.core.container.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.connected.ServerIssueLocation;
import org.sonarsource.sonarlint.core.proto.Sonarlint.ServerIssue.TextRange;

public class DefaultServerLocation implements ServerIssueLocation {
  private final String message;
  private final String filePath;
  private final String codeSnippet;
  private final org.sonarsource.sonarlint.core.analysis.api.TextRange textRange;

  public DefaultServerLocation(@Nullable String filePath, @Nullable TextRange textRange, @Nullable String message, @Nullable String codeSnippet) {
    this.textRange = textRange != null ? convert(textRange) : null;
    this.filePath = filePath;
    this.message = message;
    this.codeSnippet = codeSnippet;
  }

  public static org.sonarsource.sonarlint.core.analysis.api.TextRange convert(TextRange serverStorageTextRange) {
    return new org.sonarsource.sonarlint.core.analysis.api.TextRange(
      serverStorageTextRange.getStartLine(),
      serverStorageTextRange.getStartLineOffset(),
      serverStorageTextRange.getEndLine(),
      serverStorageTextRange.getEndLineOffset());
  }

  @Override
  public String getFilePath() {
    return filePath;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getCodeSnippet() {
    return codeSnippet;
  }

  @CheckForNull
  @Override
  public org.sonarsource.sonarlint.core.analysis.api.TextRange getTextRange() {
    return textRange;
  }
}
