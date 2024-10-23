package com.troblecodings.signals.core;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;

public class BlockPosSignalHolder {

    public final BlockPos pos;
    private boolean turnSignalOff;

    public BlockPosSignalHolder(final BlockPos pos) {
        this(pos, false);
    }

    public BlockPosSignalHolder(final BlockPos pos, final boolean turnSignalOff) {
        this.pos = pos;
        this.turnSignalOff = turnSignalOff;
    }

    public void setTurnSignalOff() {
        turnSignalOff = true;
    }

    public boolean shouldTurnSignalOff() {
        return turnSignalOff;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, turnSignalOff);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final BlockPosSignalHolder other = (BlockPosSignalHolder) obj;
        return Objects.equals(pos, other.pos) && turnSignalOff == other.turnSignalOff;
    }

}