package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.BufferFactory;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;

import net.minecraft.world.level.block.Rotation;

public class ModeSet implements INetworkSavable {

    private static final String MODE = "mode";
    private static final String ROTATION = "rotation";

    public EnumGuiMode mode;
    public Rotation rotation;

    public ModeSet(final NBTWrapper compound) {
        this.read(Objects.requireNonNull(compound));
    }

    public ModeSet(final EnumGuiMode mode, final Rotation rotation) {
        this.mode = Objects.requireNonNull(mode);
        this.rotation = Objects.requireNonNull(rotation);
    }

    private ModeSet(final BufferFactory buffer) {
        readNetwork(buffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, rotation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final ModeSet other = (ModeSet) obj;
        return mode == other.mode && rotation == other.rotation;
    }

    @Override
    public String toString() {
        return "ModeSet [mode=" + mode + ", rotation=" + rotation + "]";
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putString(MODE, this.mode.name());
        tag.putString(ROTATION, this.rotation.name());
    }

    @Override
    public void read(final NBTWrapper tag) {
        this.mode = EnumGuiMode.valueOf(tag.getString(MODE));
        this.rotation = Rotation.valueOf(tag.getString(ROTATION));
    }

    @Override
    public void readNetwork(final BufferFactory buffer) {
        this.mode = EnumGuiMode.values()[buffer.getByteAsInt()];
        this.rotation = Rotation.values()[buffer.getByteAsInt()];
    }

    @Override
    public void writeNetwork(final BufferFactory buffer) {
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
    }

    public static ModeSet of(final BufferFactory buffer) {
        return new ModeSet(buffer);
    }
}