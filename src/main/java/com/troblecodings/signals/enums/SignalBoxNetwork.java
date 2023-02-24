package com.troblecodings.signals.enums;

import java.nio.ByteBuffer;

public enum SignalBoxNetwork {

    SEND_POS_ENTRY(false), SEND_INT_ENTRY(false), REMOVE_ENTRY(false), REQUEST_PW(false),
    REMOVE_POS(false), RESET_PW(false), SEND_GRID(true), SEND_PW_UPDATE(true);

    private final boolean isForClientSide;

    private SignalBoxNetwork(final boolean isForClientSide) {
        this.isForClientSide = isForClientSide;
    }

    public static SignalBoxNetwork of(final ByteBuffer buffer) {
        return SignalBoxNetwork.values()[Byte.toUnsignedInt(buffer.get())];
    }

    public boolean isForClientSide() {
        return isForClientSide;
    }

}
