package com.troblecodings.signals.handler;

import java.util.Objects;

import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.StateInfo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalStateInfo {

    public final BlockPos pos;
    public final World world;
    public final Signal signal;

    public SignalStateInfo(final World world, final BlockPos pos, final Signal signal) {
        this.pos = pos;
        this.world = world;
        this.signal = signal;
    }

    public boolean isWorldNullOrClientSide() {
        return world == null || world.isClientSide;
    }

    public boolean isValid() {
        return pos != null && world != null && signal != null;
    }

    public StateInfo toStateInfo() {
        return new StateInfo(world, pos);
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
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalStateInfo other = (SignalStateInfo) obj;
        return Objects.equals(pos, other.pos) && Objects.equals(signal, other.signal)
                && Objects.equals(world, other.world);
    }
}