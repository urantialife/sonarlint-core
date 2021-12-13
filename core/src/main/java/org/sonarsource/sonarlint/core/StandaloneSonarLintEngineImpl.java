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
package org.sonarsource.sonarlint.core;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonarsource.sonarlint.core.client.api.common.PluginDetails;
import org.sonarsource.sonarlint.core.client.api.common.analysis.AnalysisResults;
import org.sonarsource.sonarlint.core.client.api.common.analysis.IssueListener;
import org.sonarsource.sonarlint.core.client.api.exceptions.SonarLintWrappedException;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneAnalysisConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneGlobalConfiguration;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneRuleDetails;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput;
import org.sonarsource.sonarlint.core.commons.progress.ClientProgressMonitor;
import org.sonarsource.sonarlint.core.commons.progress.ProgressMonitor;
import org.sonarsource.sonarlint.core.container.module.ModuleRegistry;
import org.sonarsource.sonarlint.core.container.standalone.StandaloneGlobalContainer;
import org.sonarsource.sonarlint.core.plugin.cache.PluginCache;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository;
import org.sonarsource.sonarlint.core.plugin.commons.PluginInstancesRepository.Configuration;
import org.sonarsource.sonarlint.core.plugin.commons.loading.PluginLocation;

import static java.util.Objects.requireNonNull;

public final class StandaloneSonarLintEngineImpl extends AbstractSonarLintEngine implements StandaloneSonarLintEngine {

  private final StandaloneGlobalConfiguration globalConfig;
  private StandaloneGlobalContainer globalContainer;

  public StandaloneSonarLintEngineImpl(StandaloneGlobalConfiguration globalConfig) {
    super(globalConfig.getLogOutput());
    this.globalConfig = globalConfig;
    start();
  }

  public StandaloneGlobalContainer getGlobalContainer() {
    return globalContainer;
  }

  @Override
  protected ModuleRegistry getModuleRegistry() {
    return getGlobalContainer().getModuleRegistry();
  }

  public void start() {
    setLogging(null);
    rwl.writeLock().lock();

    var pluginInstancesRepository = createPluginInstancesRepository();

    this.globalContainer = new StandaloneGlobalContainer(globalConfig, pluginInstancesRepository);
    try {
      globalContainer.startComponents();
    } catch (RuntimeException e) {
      throw SonarLintWrappedException.wrap(e);
    } finally {
      rwl.writeLock().unlock();
    }
  }

  private PluginInstancesRepository createPluginInstancesRepository() {
    Path cacheDir = globalConfig.getSonarLintUserHome().resolve("plugins");
    var fileCache = PluginCache.create(cacheDir);

    var plugins = globalConfig.getPluginUrls().stream()
      .map(fileCache::getFromCacheOrCopy)
      .map(r -> fileCache.get(r.getFilename(), r.getHash()))
      .map(p -> new PluginLocation(p, true))
      .collect(Collectors.toList());

    var config = new Configuration(plugins, globalConfig.getEnabledLanguages(), Optional.ofNullable(globalConfig.getNodeJsVersion()));
    return new PluginInstancesRepository(config);
  }

  @Override
  public Optional<StandaloneRuleDetails> getRuleDetails(String ruleKey) {
    return Optional.ofNullable(globalContainer.getRuleDetails(ruleKey));
  }

  @Override
  public Collection<StandaloneRuleDetails> getAllRuleDetails() {
    return globalContainer.getAllRuleDetails();
  }

  @Override
  public AnalysisResults analyze(StandaloneAnalysisConfiguration configuration, IssueListener issueListener, @Nullable ClientLogOutput logOutput,
    @Nullable ClientProgressMonitor monitor) {
    requireNonNull(configuration);
    requireNonNull(issueListener);
    setLogging(logOutput);
    rwl.readLock().lock();
    return withModule(configuration, moduleContainer -> {
      try {
        return globalContainer.analyze(moduleContainer, configuration, issueListener, new ProgressMonitor(monitor));
      } catch (RuntimeException e) {
        throw SonarLintWrappedException.wrap(e);
      } finally {
        rwl.readLock().unlock();
      }
    });
  }

  @Override
  public void stop() {
    setLogging(null);
    rwl.writeLock().lock();
    try {
      if (globalContainer == null) {
        return;
      }
      globalContainer.stopComponents(false);
    } catch (RuntimeException e) {
      throw SonarLintWrappedException.wrap(e);
    } finally {
      this.globalContainer = null;
      rwl.writeLock().unlock();
    }
  }

  @Override
  public Collection<PluginDetails> getPluginDetails() {
    setLogging(null);
    rwl.readLock().lock();
    try {
      return globalContainer.getPluginDetails();
    } finally {
      rwl.readLock().unlock();
    }
  }

}
