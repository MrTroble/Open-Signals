package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.ReadBuffer;

public enum SignalControllerNetwork {

    SEND_MODE, SEND_RS_PROFILE, SEND_PROPERTY, SET_PROFILE, REMOVE_PROPERTY, REMOVE_PROFILE;

    public static SignalControllerNetwork of(final ReadBuffer buffer) {
        return values()[buffer.getByteAsInt()];
    }

}