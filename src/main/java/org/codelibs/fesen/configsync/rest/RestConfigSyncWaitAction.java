package org.codelibs.fesen.configsync.rest;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.codelibs.fesen.action.ActionListener.wrap;
import static org.codelibs.fesen.rest.RestRequest.Method.GET;

import java.io.IOException;
import java.util.List;

import org.codelibs.fesen.ElasticsearchException;
import org.codelibs.fesen.client.node.NodeClient;
import org.codelibs.fesen.common.inject.Inject;
import org.codelibs.fesen.common.settings.Settings;
import org.codelibs.fesen.configsync.service.ConfigSyncService;
import org.codelibs.fesen.rest.RestController;
import org.codelibs.fesen.rest.RestRequest;

public class RestConfigSyncWaitAction extends RestConfigSyncAction {

    private final ConfigSyncService configSyncService;

    @Inject
    public RestConfigSyncWaitAction(final Settings settings, final RestController controller, final ConfigSyncService configSyncService) {
        this.configSyncService = configSyncService;
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(new Route(GET, "/_configsync/wait")));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        try {
            switch (request.method()) {
            case GET:
                final String status = request.param("status", "yellow");
                final String timeout = request.param("timeout", "30s");
                return channel -> configSyncService.waitForStatus(status, timeout,
                        wrap(response -> sendResponse(channel, null), e -> sendErrorResponse(channel, e)));
            default:
                return channel -> sendErrorResponse(channel, new ElasticsearchException("Unknown request type."));
            }
        } catch (final Exception e) {
            return channel -> sendErrorResponse(channel, e);
        }
    }

    @Override
    public String getName() {
        return "configsync_wait_action";
    }
}
