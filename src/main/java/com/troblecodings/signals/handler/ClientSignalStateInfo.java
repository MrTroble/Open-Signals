package com.troblecodings.signals.handler;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClientSignalStateInfo extends SignalStateInfo {

    public ClientSignalStateInfo(final World world, final BlockPos pos) {
        super(world, pos, null);
    }

    public ClientSignalStateInfo(final SignalStateInfo info) {
        super(info.world, info.pos, null);
    }

    @Override
    public String toString() {
        return "ClientSignalStateInfo [world=" + world + ", pos=" + pos + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, world);
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
        return Objects.equals(pos, other.pos) && Objects.equals(world, other.world);
    }
}