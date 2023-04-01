package com.troblecodings.signals.enums;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.BufferFactory;

public enum LinkType implements NamableWrapper {
    SIGNAL("signal"), INPUT("input"), OUTPUT("output");

    private static final String LINK_TYPE = "linkType";
    private String name;

    private LinkType(final String name) {
        this.name = name;
    }

    public void write(final NBTWrapper wrapper) {
        wrapper.putString(LINK_TYPE, name);
    }

    @Override
    public String getNameWrapper() {
        return this.name;
    }

    public static LinkType of(final String name) {
        for (final LinkType type : values())
            if (type.name.equalsIgnoreCase(name))
                return type;
        return null;
    }

    public static LinkType of(final NBTWrapper wrapper) {
        return of(wrapper.getString(LINK_TYPE));
    }

    public static LinkType of(final BufferFactory buffer) {
        return values()[buffer.getByteAsInt()];
    }
}