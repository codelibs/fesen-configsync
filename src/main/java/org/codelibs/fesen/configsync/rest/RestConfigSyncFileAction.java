package org.codelibs.fesen.configsync.rest;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.codelibs.fesen.action.ActionListener.wrap;
import static org.codelibs.fesen.rest.RestRequest.Method.DELETE;
import static org.codelibs.fesen.rest.RestRequest.Method.GET;
import static org.codelibs.fesen.rest.RestRequest.Method.POST;
import static org.codelibs.fesen.rest.RestStatus.NOT_FOUND;
import static org.codelibs.fesen.rest.RestStatus.OK;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codelibs.fesen.ElasticsearchException;
import org.codelibs.fesen.client.node.NodeClient;
import org.codelibs.fesen.common.bytes.BytesReference;
import org.codelibs.fesen.common.inject.Inject;
import org.codelibs.fesen.common.settings.Settings;
import org.codelibs.fesen.configsync.service.ConfigSyncService;
import org.codelibs.fesen.rest.BytesRestResponse;
import org.codelibs.fesen.rest.RestController;
import org.codelibs.fesen.rest.RestRequest;
import org.codelibs.fesen.search.lookup.SourceLookup;
import org.codelibs.fesen.search.sort.SortOrder;

public class RestConfigSyncFileAction extends RestConfigSyncAction {

    private final ConfigSyncService configSyncService;

    @Inject
    public RestConfigSyncFileAction(final Settings settings, final RestController controller, final ConfigSyncService configSyncService) {
        this.configSyncService = configSyncService;
    }

    @Override
    public List<Route> routes() {
        return unmodifiableList(
                asList(new Route(GET, "/_configsync/file"), new Route(POST, "/_configsync/file"), new Route(DELETE, "/_configsync/file")));
    }

    @Override
    protected RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        try {
            final BytesReference content = request.content();
            switch (request.method()) {
            case GET: {
                final String path;
                if (request.param(ConfigSyncService.PATH) != null) {
                    path = request.param(ConfigSyncService.PATH);
                } else if (content != null && content.length() > 0) {
                    final Map<String, Object> sourceAsMap = SourceLookup.sourceAsMap(content);
                    path = (String) sourceAsMap.get(ConfigSyncService.PATH);
                } else {
                    path = null;
                }
                if (path == null) {
                    final String[] sortValues = request.param("sort", ConfigSyncService.PATH).split(":");
                    final String sortField;
                    final String sortOrder;
                    if (sortValues.length > 1) {
                        sortField = sortValues[0];
                        sortOrder = sortValues[1];
                    } else {
                        sortField = sortValues[0];
                        sortOrder = SortOrder.ASC.toString();
                    }

                    final String[] fields = request.paramAsStringArrayOrEmptyIfAll("fields");
                    final int from = request.paramAsInt("from", 0);
                    final int size = request.paramAsInt("size", 10);
                    return channel -> configSyncService.getPaths(from, size, fields, sortField, sortOrder, wrap(response -> {
                        final Map<String, Object> params = new HashMap<>();
                        params.put(fields.length == 0 ? "path" : "file", response);
                        sendResponse(channel, params);
                    }, e -> sendErrorResponse(channel, e)));
                }
                return channel -> configSyncService.getContent(path, wrap(configContent -> {
                    if (configContent != null) {
                        channel.sendResponse(new BytesRestResponse(OK, "application/octet-stream", configContent));
                    } else {
                        channel.sendResponse(new BytesRestResponse(NOT_FOUND, path + " is not found."));
                    }
                }, e -> sendErrorResponse(channel, e)));
            }
            case POST: {
                if (content == null) {
                    throw new ElasticsearchException("content is empty.");
                }
                final String path;
                byte[] contentArray;
                if (request.param(ConfigSyncService.PATH) != null) {
                    path = request.param(ConfigSyncService.PATH);
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        content.writeTo(out);
                        contentArray = out.toByteArray();
                    }
                } else {
                    final Map<String, Object> sourceAsMap = SourceLookup.sourceAsMap(content);
                    path = (String) sourceAsMap.get(ConfigSyncService.PATH);
                    final String fileContent = (String) sourceAsMap.get(ConfigSyncService.CONTENT);
                    contentArray = Base64.getDecoder().decode(fileContent);
                }
                return channel -> configSyncService.store(path, contentArray,
                        wrap(res -> sendResponse(channel, null), e -> sendErrorResponse(channel, e)));
            }
            case DELETE: {
                final String path;
                if (request.param(ConfigSyncService.PATH) != null) {
                    path = request.param(ConfigSyncService.PATH);
                } else if (content != null && content.length() > 0) {
                    final Map<String, Object> sourceAsMap = SourceLookup.sourceAsMap(content);
                    path = (String) sourceAsMap.get(ConfigSyncService.PATH);
                } else {
                    path = null;
                }
                if (path == null) {
                    return channel -> sendErrorResponse(channel, new ElasticsearchException(ConfigSyncService.PATH + " is empty."));
                }
                return channel -> configSyncService.delete(path, wrap(response -> {
                    final Map<String, Object> params = new HashMap<>();
                    params.put("result", response.getResult().toString().toLowerCase());
                    sendResponse(channel, params);
                }, e -> sendErrorResponse(channel, e)));
            }
            default:
                return channel -> sendErrorResponse(channel, new ElasticsearchException("Unknown request type."));
            }
        } catch (final Exception e) {
            return channel -> sendErrorResponse(channel, e);
        }
    }

    @Override
    public String getName() {
        return "configsync_file_action";
    }
}
