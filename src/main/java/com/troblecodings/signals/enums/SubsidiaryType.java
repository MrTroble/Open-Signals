package com.troblecodings.signals.enums;

import java.nio.ByteBuffer;

import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.BufferBuilder;

public enum SubsidiaryType implements NamableWrapper {

    ZS1("zs1"), ZS7("zs7"), ZS8("zs8");

    private final String name;

    private SubsidiaryType(final String name) {
        this.name = name;
    }

    @Override
    public String getNameWrapper() {
        return name;
    }

    public static SubsidiaryType of(final ByteBuffer buffer) {
        return values()[Byte.toUnsignedInt(buffer.get())];
    }

    public void writeNetwork(final BufferBuilder buffer) {
        buffer.putByte((byte) this.ordinal());
    }
}