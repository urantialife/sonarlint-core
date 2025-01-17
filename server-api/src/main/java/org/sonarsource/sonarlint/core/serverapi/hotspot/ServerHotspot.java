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
package org.sonarsource.sonarlint.core.serverapi.hotspot;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class ServerHotspot {
  public ServerHotspot(String message,
    String filePath,
    TextRange textRange,
    String author,
    Status status,
    @Nullable Resolution resolution,
    Rule rule,
    @Nullable String codeSnippet) {
    this.message = message;
    this.filePath = filePath;
    this.textRange = textRange;
    this.author = author;
    this.status = status;
    this.resolution = resolution;
    this.rule = rule;
    this.codeSnippet = codeSnippet;
  }

  public final String message;
  public final String filePath;
  public final TextRange textRange;
  public final String author;
  public final Status status;
  public final Resolution resolution;
  public final Rule rule;
  @CheckForNull
  public final String codeSnippet;

  public static class Rule {

    public final String key;
    public final String name;
    public final String securityCategory;
    public final Probability vulnerabilityProbability;
    public final String riskDescription;
    public final String vulnerabilityDescription;
    public final String fixRecommendations;

    public Rule(String key,
      String name,
      String securityCategory,
      Probability vulnerabilityProbability,
      String riskDescription,
      String vulnerabilityDescription,
      String fixRecommendations) {

      this.key = key;
      this.name = name;
      this.securityCategory = securityCategory;
      this.vulnerabilityProbability = vulnerabilityProbability;
      this.riskDescription = riskDescription;
      this.vulnerabilityDescription = vulnerabilityDescription;
      this.fixRecommendations = fixRecommendations;
    }

    public enum Probability {
      HIGH, MEDIUM, LOW
    }
  }

  public enum Status {
    TO_REVIEW("To review"), REVIEWED("Reviewed");

    Status(String description) {
      this.description = description;
    }

    public final String description;
  }

  public enum Resolution {
    FIXED("fixed"), SAFE("safe");

    Resolution(String description) {
      this.description = description;
    }

    public final String description;
  }

  public static class TextRange {
    private final Integer startLine;
    private final Integer startLineOffset;
    private final Integer endLine;
    private final Integer endLineOffset;

    public TextRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
      this.startLine = startLine;
      this.startLineOffset = startLineOffset;
      this.endLine = endLine;
      this.endLineOffset = endLineOffset;
    }

    @CheckForNull
    public Integer getStartLine() {
      return startLine;
    }

    @CheckForNull
    public Integer getStartLineOffset() {
      return startLineOffset;
    }

    @CheckForNull
    public Integer getEndLine() {
      return endLine;
    }

    @CheckForNull
    public Integer getEndLineOffset() {
      return endLineOffset;
    }
  }
}
