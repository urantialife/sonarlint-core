/*
 * SonarLint Core - Implementation
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
package org.sonarsource.sonarlint.core.client.api.common;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.scanner.fs.ProjectFileEvent;
import org.sonar.api.scanner.fs.ProjectFileListener;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarsource.sonarlint.core.container.analysis.filesystem.InputFileBuilder;

public class ProjectFileEventWatcher {

  private static final Logger LOG = Loggers.get(ProjectFileEventWatcher.class);

  private final List<ProjectFileListener> listeners;
  private final InputFileBuilder inputFileBuilder;

  public ProjectFileEventWatcher(InputFileBuilder inputFileBuilder) {
    this(new ProjectFileListener[0], inputFileBuilder);
  }

  public ProjectFileEventWatcher(ProjectFileListener[] listeners, InputFileBuilder inputFileBuilder) {
    this.listeners = Arrays.asList(listeners);
    this.inputFileBuilder = inputFileBuilder;
  }

  public void fireProjectFileEvent(ClientProjectFileEvent event) {
    ProjectFileEvent apiEvent = DefaultProjectFileEvent.of(inputFileBuilder.create(event.target()), event.type());
    listeners.forEach(l -> this.tryFireProjectFileEvent(l, apiEvent));
  }

  private void tryFireProjectFileEvent(ProjectFileListener listener, ProjectFileEvent event) {
    try {
      listener.process(event);
    } catch(Throwable t) {
      LOG.error("Error processing file event", t);
    }
  }
}
