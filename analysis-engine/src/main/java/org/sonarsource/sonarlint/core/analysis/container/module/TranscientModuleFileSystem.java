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
package org.sonarsource.sonarlint.core.analysis.container.module;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.sonarsource.sonarlint.core.analysis.api.ClientFileSystem.FileType;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;

class TranscientModuleFileSystem implements ModuleFileSystem {

  private final Iterable<ClientInputFile> filesToAnalyze;

  TranscientModuleFileSystem(Iterable<ClientInputFile> filesToAnalyze) {
    this.filesToAnalyze = filesToAnalyze;
  }

  @Override
  public Stream<ClientInputFile> listFiles(String suffix, FileType type) {
    return listAllFiles()
      .filter(file -> file.relativePath().endsWith(suffix))
      .filter(file -> file.isTest() == (type == FileType.TEST));
  }

  @Override
  public Stream<ClientInputFile> listAllFiles() {
    return StreamSupport.stream(filesToAnalyze.spliterator(), false);
  }
}
