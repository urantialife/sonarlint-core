/*
 * SonarLint Core - Analysis Engine
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
package org.sonarsource.sonarlint.core.analysis.sonarapi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.rule.RuleKey;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFileEdit;
import org.sonarsource.sonarlint.core.analysis.api.QuickFix;
import org.sonarsource.sonarlint.core.analysis.api.QuickFixable;
import org.sonarsource.sonarlint.core.analysis.api.TextEdit;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.SonarLintInputDir;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.SonarLintInputFile;
import org.sonarsource.sonarlint.core.analysis.container.analysis.filesystem.SonarLintInputProject;
import org.sonarsource.sonarlint.plugin.api.issue.NewInputFileEdit;
import org.sonarsource.sonarlint.plugin.api.issue.NewQuickFix;
import testutils.TestInputFileBuilder;

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultSonarLintIssueTests {

  private SonarLintInputProject project;

  private final InputFile inputFile = new TestInputFileBuilder("src/Foo.php")
    .initMetadata("Foo\nBar\n")
    .build();

  @TempDir
  private Path baseDir;

  @BeforeEach
  void prepare() throws IOException {
    project = new SonarLintInputProject();
  }

  @Test
  void build_file_issue() {
    var storage = mock(SensorStorage.class);
    var range = inputFile.selectLine(1);
    var issue = new DefaultSonarLintIssue(project, baseDir, storage)
      .at(new DefaultSonarLintIssueLocation()
        .on(inputFile)
        .at(range)
        .message("Wrong way!"))
      .forRule(RuleKey.of("repo", "rule"))
      .gap(10.0);

    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(inputFile);
    assertThat(issue.ruleKey()).isEqualTo(RuleKey.of("repo", "rule"));
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(1);
    assertThat(issue.primaryLocation().message()).isEqualTo("Wrong way!");

    assertThatExceptionOfType(UnsupportedOperationException.class)
      .isThrownBy(() -> issue.gap())
      .withMessage("No gap in SonarLint");

    var newQuickFix = issue.newQuickFix().message("Fix this issue");
    var newInputFileEdit = newQuickFix.newInputFileEdit().on(inputFile);
    newInputFileEdit.addTextEdit(newInputFileEdit.newTextEdit().at(range).withNewText("// Fixed!"));
    newQuickFix.addInputFileEdit(newInputFileEdit);
    issue.addQuickFix(newQuickFix);

    var quickFixes = ((QuickFixable) issue).quickFixes();
    assertThat(quickFixes).hasSize(1);
    var quickFix = quickFixes.get(0);
    assertThat(quickFix.message()).isEqualTo("Fix this issue");
    var inputFileEdits = quickFix.inputFileEdits();
    assertThat(inputFileEdits).hasSize(1);
    var inputFileEdit = inputFileEdits.get(0);
    assertThat(inputFileEdit.target()).isEqualTo(((SonarLintInputFile) inputFile).getClientInputFile());
    assertThat(inputFileEdit.textEdits()).hasSize(1);
    var textEdit = inputFileEdit.textEdits().get(0);
    assertThat(textEdit.range().getStartLine()).isEqualTo(range.start().line());
    assertThat(textEdit.range().getStartLineOffset()).isEqualTo(range.start().lineOffset());
    assertThat(textEdit.range().getEndLine()).isEqualTo(range.end().line());
    assertThat(textEdit.range().getEndLineOffset()).isEqualTo(range.end().lineOffset());
    assertThat(textEdit.newText()).isEqualTo("// Fixed!");

    issue.save();

    verify(storage).store(issue);
  }

  @Test
  void replace_null_characters() {
    var storage = mock(SensorStorage.class);
    var issue = new DefaultSonarLintIssue(project, baseDir, storage)
      .at(new DefaultSonarLintIssueLocation()
        .on(inputFile)
        .message("Wrong \u0000 use of NULL\u0000"))
      .forRule(RuleKey.of("repo", "rule"));

    assertThat(issue.primaryLocation().message()).isEqualTo("Wrong [NULL] use of NULL[NULL]");

    issue.save();

    verify(storage).store(issue);
  }

  @Test
  void truncate_and_trim() {
    var storage = mock(SensorStorage.class);
    var prefix = "prefix: ";
    var issue = new DefaultSonarLintIssue(project, baseDir, storage)
      .at(new DefaultSonarLintIssueLocation()
        .on(inputFile)
        .message("   " + prefix + repeat("a", 4000)))
      .forRule(RuleKey.of("repo", "rule"));

    var ellipse = "...";
    assertThat(issue.primaryLocation().message()).isEqualTo(prefix + repeat("a", 4000 - prefix.length() - ellipse.length()) + ellipse);

    issue.save();

    verify(storage).store(issue);
  }

  @Test
  void move_directory_issue_to_project_root() {
    var storage = mock(SensorStorage.class);
    var issue = new DefaultSonarLintIssue(project, baseDir, storage)
      .at(new DefaultSonarLintIssueLocation()
        .on(new SonarLintInputDir(baseDir.resolve("src/main")))
        .message("Wrong way!"))
      .forRule(RuleKey.of("repo", "rule"))
      .overrideSeverity(Severity.BLOCKER);

    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(project);
    assertThat(issue.ruleKey()).isEqualTo(RuleKey.of("repo", "rule"));
    assertThat(issue.primaryLocation().textRange()).isNull();
    assertThat(issue.primaryLocation().message()).isEqualTo("[src/main] Wrong way!");
    assertThat(issue.overriddenSeverity()).isEqualTo(Severity.BLOCKER);

    issue.save();

    verify(storage).store(issue);
  }

  @Test
  void build_project_issue() throws IOException {
    var storage = mock(SensorStorage.class);
    var issue = new DefaultSonarLintIssue(project, baseDir, storage)
      .at(new DefaultSonarLintIssueLocation()
        .on(project)
        .message("Wrong way!"))
      .forRule(RuleKey.of("repo", "rule"))
      .gap(10.0);

    assertThat(issue.primaryLocation().inputComponent()).isEqualTo(project);
    assertThat(issue.ruleKey()).isEqualTo(RuleKey.of("repo", "rule"));
    assertThat(issue.primaryLocation().textRange()).isNull();
    assertThat(issue.primaryLocation().message()).isEqualTo("Wrong way!");

    issue.save();

    verify(storage).store(issue);
  }

}
