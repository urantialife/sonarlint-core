/*
 * SonarLint Core - Client API
 * Copyright (C) 2009-2020 SonarSource SA
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
package org.sonarsource.sonarlint.core.client.api.connected;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.common.ProgressMonitor;

public interface WsHelper {
  /**
   * Checks if it is possible to reach server with provided configuration
   * @since 2.1
   */
  ValidationResult validateConnection(ServerConfiguration serverConfig);

  /**
   * Returns the list of remote organizations
   */
  List<RemoteOrganization> listOrganizations(ServerConfiguration serverConfig, @Nullable ProgressMonitor monitor);

  /**
   * Returns the list of remote organizations where the user is a member.
   * @since 3.5
   */
  List<RemoteOrganization> listUserOrganizations(ServerConfiguration serverConfig, @Nullable ProgressMonitor monitor);

  /**
   * Get an organization.
   * @return null if the organization is not found
   * @since 3.5
   */
  Optional<RemoteOrganization> getOrganization(ServerConfiguration serverConfig, String organizationKey, ProgressMonitor monitor);

  /**
   * Get a project.
   * @since 4.0
   */
  Optional<RemoteProject> getProject(ServerConfiguration serverConfig, String projectKey, ProgressMonitor monitor);
}
