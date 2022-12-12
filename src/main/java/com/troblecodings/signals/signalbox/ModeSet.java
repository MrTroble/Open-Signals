package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.entrys.ISaveable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;

public class ModeSet implements ISaveable {

    private static final String MODE = "mode";
    private static final String ROTATION = "rotation";

    public EnumGuiMode mode;
    public Rotation rotation;

    public ModeSet(final CompoundTag compound) {
        this.read(Objects.requireNonNull(compound));
    }

    public ModeSet(final EnumGuiMode mode, final Rotation rotation) {
        this.mode = Objects.requireNonNull(mode);
        this.rotation = Objects.requireNonNull(rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, rotation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ModeSet other = (ModeSet) obj;
        return mode == other.mode && rotation == other.rotation;
    }

    @Override
    public String toString() {
        return "ModeSet [mode=" + mode + ", rotation=" + rotation + "]";
    }

    @Override
    public void write(final CompoundTag tag) {
        tag.putString(MODE, this.mode.name());
        tag.putString(ROTATION, this.rotation.name());
    }

    @Override
    public void read(final CompoundTag tag) {
        this.mode = EnumGuiMode.valueOf(tag.getString(MODE));
        this.rotation = Rotation.valueOf(tag.getString(ROTATION));
    }

}
