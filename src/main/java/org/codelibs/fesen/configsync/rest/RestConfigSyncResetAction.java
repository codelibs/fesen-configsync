package org.codelibs.fesen.configsync.rest;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.codelibs.fesen.action.ActionListener.wrap;
import static org.codelibs.fesen.rest.RestRequest.Method.POST;

import java.io.IOException;
import java.util.List;

import org.codelibs.fesen.ElasticsearchException;
import org.codelibs.fesen.client.node.NodeClient;
import org.codelibs.fesen.common.inject.Inject;
import org.codelibs.fesen.common.settings.Settings;
import org.codelibs.fesen.configsync.service.ConfigSyncService;
import org.codelibs.fesen.rest.RestController;
import org.codelibs.fesen.rest.RestRequest;

public class RestConfigSyncResetAction extends RestConfigSyncAction {

    private final ConfigSyncService configSyncService;

    @Inject
    public RestConfigSyncResetAction(final Settings settings, final RestController controller, final ConfigSyncService configSyncService) {
        this.configSyncService = configSyncService;
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(new Route(POST, "/_configsync/reset")));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        try {
            switch (request.method()) {
            case POST:
                return channel -> configSyncService
                        .resetSync(wrap(response -> sendResponse(channel, null), e -> sendErrorResponse(channel, e)));
            default:
                return channel -> sendErrorResponse(channel, new ElasticsearchException("Unknown request type."));
            }
        } catch (final Exception e) {
            return channel -> sendErrorResponse(channel, e);
        }
    }

    @Override
    public String getName() {
        return "configsync_reset_action";
    }
}
