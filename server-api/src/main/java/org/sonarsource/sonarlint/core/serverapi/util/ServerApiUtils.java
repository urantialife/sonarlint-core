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
package org.sonarsource.sonarlint.core.serverapi.util;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonarqube.ws.Common.TextRange;

public class ServerApiUtils {

  public static String extractCodeSnippet(String sourceCode, TextRange textRange) {
    return extractCodeSnippet(sourceCode.split("\\r?\\n"), textRange);
  }

  private static String extractCodeSnippet(String[] sourceCodeLines, TextRange textRange) {
    if (textRange.getStartLine() == textRange.getEndLine()) {
      var fullline = sourceCodeLines[textRange.getStartLine() - 1];
      return fullline.substring(textRange.getStartOffset(), textRange.getEndOffset());
    } else {
      var linesOfTextRange = Arrays.copyOfRange(sourceCodeLines, textRange.getStartLine() - 1, textRange.getEndLine());
      linesOfTextRange[0] = linesOfTextRange[0].substring(textRange.getStartOffset());
      linesOfTextRange[linesOfTextRange.length - 1] = linesOfTextRange[linesOfTextRange.length - 1].substring(0, textRange.getEndOffset());
      return String.join("\n", linesOfTextRange);
    }
  }

  public static boolean isBlank(@Nullable List<?> list) {
    return list == null || list.isEmpty();
  }

  public static boolean areBlank(List<?>... lists) {
    return Arrays.stream(lists).allMatch(ServerApiUtils::isBlank);
  }

  private ServerApiUtils() {
    // utility class
  }

}
