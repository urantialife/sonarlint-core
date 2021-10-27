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
package org.sonarsource.sonarlint.core.container.analysis.issue.ignore.scanner;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class LineRange {
  private final int from;
  private final int to;

  public LineRange(int line) {
    this(line, line);
  }

  public LineRange(int from, int to) {
    this.from = from;
    this.to = to;
  }

  public boolean in(int lineId) {
    return from <= lineId && lineId <= to;
  }

  public Set<Integer> toLines() {
    Set<Integer> lines = new LinkedHashSet<>(to - from + 1);
    for (int index = from; index <= to; index++) {
      lines.add(index);
    }
    return lines;
  }

  public int from() {
    return from;
  }

  public int to() {
    return to;
  }

  @Override
  public String toString() {
    return "[" + from + "-" + to + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    if (fieldsDiffer((LineRange) obj)) {
      return false;
    }
    return true;
  }

  private boolean fieldsDiffer(LineRange other) {
    return from != other.from || to != other.to;
  }
}
