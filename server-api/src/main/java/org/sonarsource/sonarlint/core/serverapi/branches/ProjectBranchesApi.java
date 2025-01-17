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
package org.sonarsource.sonarlint.core.serverapi.branches;

import com.google.protobuf.Descriptors.EnumValueDescriptor;
import java.util.List;
import org.sonarqube.ws.Common.BranchType;
import org.sonarqube.ws.ProjectBranches;
import org.sonarqube.ws.ProjectBranches.Branch;
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger;
import org.sonarsource.sonarlint.core.serverapi.ServerApiHelper;
import org.sonarsource.sonarlint.core.serverapi.UrlUtils;

import static java.util.stream.Collectors.toList;

public class ProjectBranchesApi {
  // https://github.com/SonarSource/sonarqube/blob/8.0/sonar-ws/src/main/protobuf/ws-commons.proto#L129
  private static final long DEPRECATED_LONG_BRANCH_TYPE = 1;

  private static final SonarLintLogger LOG = SonarLintLogger.get();

  private static final String LIST_ALL_PROJECT_BRANCHES_URL = "/api/project_branches/list.protobuf";
  private final ServerApiHelper helper;

  public ProjectBranchesApi(ServerApiHelper helper) {
    this.helper = helper;
  }

  public List<ServerBranch> getAllBranches(String projectKey) {
    ProjectBranches.ListWsResponse response;
    try (var wsResponse = helper.get(LIST_ALL_PROJECT_BRANCHES_URL + "?project=" + UrlUtils.urlEncode(projectKey)); var is = wsResponse.bodyAsStream()) {
      response = ProjectBranches.ListWsResponse.parseFrom(is);
    } catch (Exception e) {
      LOG.error("Error while fetching project branches", e);
      return List.of();
    }
    return response.getBranchesList().stream()
      .filter(b -> b.getType() == BranchType.BRANCH || isLongLiving(b)
        || ((EnumValueDescriptor) b.getField(ProjectBranches.Branch.getDescriptor().findFieldByNumber(ProjectBranches.Branch.TYPE_FIELD_NUMBER)))
          .getNumber() == DEPRECATED_LONG_BRANCH_TYPE)
      .map(branchWs -> new ServerBranch(branchWs.getName(), branchWs.getIsMain())).collect(toList());
  }

  private boolean isLongLiving(Branch b) {
    if (b.getType() != BranchType.UNKNOWN_BRANCH_TYPE) {
      return false;
    }
    List<Long> values = b.getUnknownFields().getField(ProjectBranches.Branch.TYPE_FIELD_NUMBER).getVarintList();
    return values.size() == 1 && values.get(0).equals(DEPRECATED_LONG_BRANCH_TYPE);
  }

}
