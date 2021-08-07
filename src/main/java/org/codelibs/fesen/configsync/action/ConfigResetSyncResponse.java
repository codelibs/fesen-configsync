package org.codelibs.fesen.configsync.action;

import org.codelibs.fesen.action.support.master.AcknowledgedResponse;

public class ConfigResetSyncResponse extends AcknowledgedResponse {
    public ConfigResetSyncResponse(final boolean acknowledged) {
        super(acknowledged);
    }
}
