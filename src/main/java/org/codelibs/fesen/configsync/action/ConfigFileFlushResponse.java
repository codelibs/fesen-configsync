package org.codelibs.fesen.configsync.action;

import org.codelibs.fesen.action.support.master.AcknowledgedResponse;

public class ConfigFileFlushResponse extends AcknowledgedResponse {
    public ConfigFileFlushResponse(final boolean acknowledged) {
        super(acknowledged);
    }
}
