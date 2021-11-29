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
package org.sonarsource.sonarlint.core.analysis.api;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.rule.RuleKey;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.DefaultTextPointer;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.DefaultTextRange;
import org.sonarsource.sonarlint.core.analysis.sonarapi.ActiveRuleAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IssueTests {
  private ActiveRuleAdapter activeRule;
  private TextRange textRange;
  private ClientInputFile clientInputFile;

  private Issue issue;

  @BeforeEach
  public void setUp() {
    activeRule = mock(ActiveRuleAdapter.class);
    when(activeRule.ruleKey()).thenReturn(RuleKey.parse("foo:S123"));

    textRange = mock(TextRange.class);
    clientInputFile = mock(ClientInputFile.class);
  }

  @Test
  void transformIssue() {
    InputComponent currentFile = mock(InputComponent.class);
    String currentFileKey = "currentFileKey";
    when(currentFile.key()).thenReturn(currentFileKey);
    InputComponent anotherFile = mock(InputComponent.class);
    when(anotherFile.key()).thenReturn("anotherFileKey");

    textRange = new DefaultTextRange(new DefaultTextPointer(1, 2), new DefaultTextPointer(2, 3));

    issue = new Issue(activeRule, "msg", textRange, clientInputFile, Collections.emptyList(), Collections.emptyList());

    assertThat(issue.getStartLine()).isEqualTo(1);
    assertThat(issue.getStartLineOffset()).isEqualTo(2);
    assertThat(issue.getEndLine()).isEqualTo(2);
    assertThat(issue.getEndLineOffset()).isEqualTo(3);

    assertThat(issue.getMessage()).isEqualTo("msg");
    assertThat(issue.getInputFile()).isEqualTo(clientInputFile);

    assertThat(issue.getRuleKey()).isEqualTo("foo:S123");
  }

  @Test
  void nullRange() {
    issue = new Issue(activeRule, "msg", null, null, Collections.emptyList(), Collections.emptyList());

    assertThat(issue.getStartLine()).isNull();
    assertThat(issue.getStartLineOffset()).isNull();
    assertThat(issue.getEndLine()).isNull();
    assertThat(issue.getEndLineOffset()).isNull();

    assertThat(issue.flows()).isEmpty();
    assertThat(issue.quickFixes()).isEmpty();
  }
}