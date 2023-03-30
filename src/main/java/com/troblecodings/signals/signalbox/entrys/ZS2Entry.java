package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.BufferFactory;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.core.JsonEnumHolder;

public class ZS2Entry extends IPathEntry<String> {

    public static final JsonEnum ZS32 = JsonEnumHolder.PROPERTIES.get("zs32");

    private String value = "";

    @Override
    public void readNetwork(final BufferFactory buffer) {
        final int value = buffer.getByteAsInt();
        if (value == -1) {
            this.value = "";
            return;
        }
        this.value = ZS32.getObjFromID(value);

    }

    @Override
    public void writeNetwork(final BufferFactory buffer) {
        if (ZS32.isValid(value)) {
            buffer.putByte((byte) ZS32.getIDFromValue(value));
        } else {
            buffer.putByte((byte) -1);
        }
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putString(ZS32.getName(), value);
    }

    @Override
    public void read(final NBTWrapper tag) {
        value = tag.getString(ZS32.getName());
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(final String value) {
        if (ZS32.isValid(value)) {
            this.value = value;
        } else {
            this.value = "";
        }
    }
}