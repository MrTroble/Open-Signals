package com.troblecodings.signals.handler;

import java.util.Objects;

import com.troblecodings.signals.blocks.Signal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SignalStateInfo {

    public final Level world;
    public final BlockPos pos;
    public final Signal signal;

    public SignalStateInfo(final Level world, final BlockPos pos) {
        this(world, pos, (Signal) world.getBlockState(pos).getBlock());
    }

    public SignalStateInfo(final Level world, final BlockPos pos, final Signal signal) {
        super();
        this.world = world;
        this.pos = pos;
        this.signal = signal == null ? (Signal) world.getBlockState(pos).getBlock() : signal;
    }

    @Override
    public String toString() {
        return "SignalStateInfo [world=" + world + ", pos=" + pos + ", signal=" + signal + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, signal, world);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SignalStateInfo other = (SignalStateInfo) obj;
        return Objects.equals(pos, other.pos) && Objects.equals(signal, other.signal)
                && Objects.equals(world, other.world);
    }
}