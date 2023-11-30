package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;

import net.minecraft.util.Rotation;

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

    private ModeSet(final ReadBuffer buffer) {
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
    public void readNetwork(final ReadBuffer buffer) {
        this.mode = EnumGuiMode.values()[buffer.getByteToUnsignedInt()];
        this.rotation = Rotation.values()[buffer.getByteToUnsignedInt()];
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) mode.ordinal());
        buffer.putByte((byte) rotation.ordinal());
    }

    public static ModeSet of(final ReadBuffer buffer) {
        return new ModeSet(buffer);
    }
}