package com.troblecodings.signals.handler;

import java.util.Objects;

import net.minecraft.world.level.ChunkPos;

public class SignalStatePosV2 {

    public final ChunkPos file;
    public final long offset;

    public SignalStatePosV2(final ChunkPos file, final long offset) {
        this.file = file;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "SignalStatePosV2 [file=" + file + ", offset=" + offset + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalStatePosV2 other = (SignalStatePosV2) obj;
        return file.equals(other.file) && offset == other.offset;
    }
}