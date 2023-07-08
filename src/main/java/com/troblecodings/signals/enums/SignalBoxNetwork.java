package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.ReadBuffer;

public enum SignalBoxNetwork {

    SEND_POS_ENTRY, SEND_INT_ENTRY, REMOVE_ENTRY, REQUEST_PW, REMOVE_POS, RESET_PW, SEND_GRID,
    SEND_PW_UPDATE, RESET_ALL_PW, SEND_CHANGED_MODES, REQUEST_LINKED_POS, NO_PW_FOUND,
    REQUEST_SUBSIDIARY, SEND_ZS2_ENTRY, UPDATE_RS_OUTPUT, NO_OUTPUT_UPDATE, OUTPUT_UPDATE,
    RESET_SUBSIDIARY, REQUEST_AUTOPATHWAY, RESET_AUTOPATHWAY, SET_AUTOPATHWAY;

    public static SignalBoxNetwork of(final ReadBuffer buffer) {
        return values()[buffer.getByteAsInt()];
    }
}