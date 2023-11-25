package com.troblecodings.signals.enums;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.OpenSignalsMain;

public enum EnumState implements NamableWrapper {
    DISABLED("DISABLED"), OFFSTATE("OFFSTATE"), ONSTATE("ONSTATE");

    private final String name;

    private EnumState(final String str) {
        this.name = str;
    }

    @Override
    public String getNameWrapper() {
        return name;
    }

    public static EnumState of(final String name) {
        for (int i = 0; i < EnumState.values().length; i++) {
            final EnumState state = EnumState.values()[i];
            if (state.getNameWrapper().equalsIgnoreCase(name)) {
                return state;
            }
        }
        OpenSignalsMain.getLogger().error("[" + name
                + "] is not a valid state of EnumState! [DISABLED] was taken as default!");
        return EnumState.DISABLED;
    }

    public static EnumState of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }
}