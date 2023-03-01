package com.troblecodings.signals.enums;

import java.nio.ByteBuffer;

public enum SignalBoxNetwork {

    SEND_POS_ENTRY, SEND_INT_ENTRY, REMOVE_ENTRY, REQUEST_PW, REMOVE_POS, RESET_PW, SEND_GRID,
    SEND_PW_UPDATE, RESET_ALL_PW, SEND_CHANGED_MODES, REQUEST_LINKED_POS, SEND_ALL_POS;

    public static SignalBoxNetwork of(final ByteBuffer buffer) {
        return SignalBoxNetwork.values()[Byte.toUnsignedInt(buffer.get())];
    }

}
