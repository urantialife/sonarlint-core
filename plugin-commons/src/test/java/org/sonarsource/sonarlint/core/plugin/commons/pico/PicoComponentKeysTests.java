/*
 * SonarLint Core - Plugin Commons
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
package org.sonarsource.sonarlint.core.plugin.commons.pico;

import org.junit.jupiter.api.Test;
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PicoComponentKeysTests {

  PicoComponentKeys keys = new PicoComponentKeys();

  @Test
  void generate_key_of_class() {
    assertThat(keys.of(FakeComponent.class)).isEqualTo(FakeComponent.class);
  }

  @Test
  void generate_key_of_object() {
    assertThat(keys.of(new FakeComponent())).isEqualTo("org.sonarsource.sonarlint.core.plugin.commons.pico.PicoComponentKeysTests.FakeComponent-fake");
  }

  @Test
  void should_log_warning_if_toString_is_not_overridden() {
    var log = mock(SonarLintLogger.class);
    keys.of(new Object(), log);
    verifyNoInteractions(log);

    // only on non-first runs, to avoid false-positives on singletons
    keys.of(new Object(), log);
    verify(log).warn(startsWith("Bad component key"));
  }

  @Test
  void should_generate_unique_key_when_toString_is_not_overridden() {
    var key = keys.of(new WrongToStringImpl());
    assertThat(key).isNotEqualTo(WrongToStringImpl.KEY);

    var key2 = keys.of(new WrongToStringImpl());
    assertThat(key2).isNotEqualTo(key);
  }

  static class FakeComponent {
    @Override
    public String toString() {
      return "fake";
    }
  }

  static class WrongToStringImpl {
    static final String KEY = "my.Component@123a";

    @Override
    public String toString() {
      return KEY;
    }
  }
}
