package com.troblecodings.signals.enums;

import com.troblecodings.signals.core.ReadBuffer;

public enum EnumMode {
    MANUELL, SINGLE, RS_INPUT;

    public static EnumMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteAsInt()];
    }
}