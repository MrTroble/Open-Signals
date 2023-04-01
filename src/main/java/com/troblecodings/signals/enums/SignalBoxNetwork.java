package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.BufferFactory;

public enum SignalBoxNetwork {

    SEND_POS_ENTRY, SEND_INT_ENTRY, REMOVE_ENTRY, REQUEST_PW, REMOVE_POS, RESET_PW, SEND_GRID,
    SEND_PW_UPDATE, RESET_ALL_PW, SEND_CHANGED_MODES, REQUEST_LINKED_POS, NO_PW_FOUND,
    REQUEST_SUBSIDIARY, SEND_ZS2_ENTRY;

    public static SignalBoxNetwork of(final BufferFactory buffer) {
        return values()[buffer.getByteAsInt()];
    }
}