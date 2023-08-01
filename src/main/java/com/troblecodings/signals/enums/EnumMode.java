package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.ReadBuffer;

public enum EnumMode {
    MANUELL, SINGLE, MUX;

    public static EnumMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteAsInt()];
    }
}