package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.ReadBuffer;

public enum SignalControllerNetwork {

    SEND_MODE, SEND_RS_PROFILE, SEND_PROPERTY, SET_PROFILE, REMOVE_PROPERTY, REMOVE_PROFILE,
    SET_RS_INPUT_PROFILE, REMOVE_RS_INPUT_PROFILE, UNLINK_INPUT_POS;

    public static SignalControllerNetwork of(final ReadBuffer buffer) {
        return values()[buffer.getByteAsInt()];
    }
}