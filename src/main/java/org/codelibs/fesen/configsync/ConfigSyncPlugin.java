package org.codelibs.fesen.configsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.codelibs.fesen.client.Client;
import org.codelibs.fesen.cluster.metadata.IndexNameExpressionResolver;
import org.codelibs.fesen.cluster.node.DiscoveryNodes;
import org.codelibs.fesen.cluster.service.ClusterService;
import org.codelibs.fesen.common.component.LifecycleComponent;
import org.codelibs.fesen.common.io.stream.NamedWriteableRegistry;
import org.codelibs.fesen.common.settings.ClusterSettings;
import org.codelibs.fesen.common.settings.IndexScopedSettings;
import org.codelibs.fesen.common.settings.Setting;
import org.codelibs.fesen.common.settings.Settings;
import org.codelibs.fesen.common.settings.SettingsFilter;
import org.codelibs.fesen.common.xcontent.NamedXContentRegistry;
import org.codelibs.fesen.configsync.rest.RestConfigSyncFileAction;
import org.codelibs.fesen.configsync.rest.RestConfigSyncFlushAction;
import org.codelibs.fesen.configsync.rest.RestConfigSyncResetAction;
import org.codelibs.fesen.configsync.rest.RestConfigSyncWaitAction;
import org.codelibs.fesen.configsync.service.ConfigSyncService;
import org.codelibs.fesen.env.Environment;
import org.codelibs.fesen.env.NodeEnvironment;
import org.codelibs.fesen.indices.SystemIndexDescriptor;
import org.codelibs.fesen.plugins.ActionPlugin;
import org.codelibs.fesen.plugins.Plugin;
import org.codelibs.fesen.plugins.SystemIndexPlugin;
import org.codelibs.fesen.repositories.RepositoriesService;
import org.codelibs.fesen.rest.RestController;
import org.codelibs.fesen.rest.RestHandler;
import org.codelibs.fesen.script.ScriptService;
import org.codelibs.fesen.threadpool.ThreadPool;
import org.codelibs.fesen.watcher.ResourceWatcherService;

public class ConfigSyncPlugin extends Plugin implements ActionPlugin, SystemIndexPlugin {

    private final PluginComponent pluginComponent = new PluginComponent();

    @Override
    public List<RestHandler> getRestHandlers(final Settings settings, final RestController restController, final ClusterSettings clusterSettings,
            final IndexScopedSettings indexScopedSettings, final SettingsFilter settingsFilter, final IndexNameExpressionResolver indexNameExpressionResolver,
            final Supplier<DiscoveryNodes> nodesInCluster) {
        final ConfigSyncService service = pluginComponent.getConfigSyncService();
        return Arrays.asList(//
                new RestConfigSyncFileAction(settings, restController, service), //
                new RestConfigSyncResetAction(settings, restController, service), //
                new RestConfigSyncFlushAction(settings, restController, service), //
                new RestConfigSyncWaitAction(settings, restController, service));
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> getGuiceServiceClasses() {
        final Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        services.add(ConfigSyncService.class);
        return services;
    }

    @Override
    public Collection<Object> createComponents(Client client, ClusterService clusterService, ThreadPool threadPool,
            ResourceWatcherService resourceWatcherService, ScriptService scriptService,
            NamedXContentRegistry xContentRegistry, Environment environment,
            NodeEnvironment nodeEnvironment, NamedWriteableRegistry namedWriteableRegistry,
            IndexNameExpressionResolver indexNameExpressionResolver,
            Supplier<RepositoriesService> repositoriesServiceSupplier) {
        final Collection<Object> components = new ArrayList<>();
        components.add(pluginComponent);
        return components;
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(//
                ConfigSyncService.INDEX_SETTING, //
                ConfigSyncService.TYPE_SETTING, //
                ConfigSyncService.XPACK_SECURITY_SETTING, //
                ConfigSyncService.CONFIG_PATH_SETTING, //
                ConfigSyncService.SCROLL_TIME_SETTING, //
                ConfigSyncService.SCROLL_SIZE_SETTING, //
                ConfigSyncService.FLUSH_INTERVAL_SETTING, //
                ConfigSyncService.FILE_UPDATER_ENABLED_SETTING//
        );
    }

    @Override
    public Collection<SystemIndexDescriptor> getSystemIndexDescriptors(Settings settings) {
        return Collections.unmodifiableList(Arrays.asList(new SystemIndexDescriptor(".configsync", "Contains config sync data")));
    }

    public static class PluginComponent {
        private ConfigSyncService configSyncService;

        public ConfigSyncService getConfigSyncService() {
            return configSyncService;
        }

        public void setConfigSyncService(final ConfigSyncService configSyncService) {
            this.configSyncService = configSyncService;
        }
    }
}
