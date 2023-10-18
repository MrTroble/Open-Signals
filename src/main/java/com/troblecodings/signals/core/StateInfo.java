package com.troblecodings.signals.core;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StateInfo {

    public final World world;
    public final BlockPos pos;

    public StateInfo(final World world, final BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public boolean worldNullOrClientSide() {
        return world == null || world.isRemote;
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
        final StateInfo other = (StateInfo) obj;
        return Objects.equals(pos, other.pos) && Objects.equals(world, other.world);
    }

    @Override
    public String toString() {
        return "StateInfo [world= " + world + ",pos=" + pos + "]";
    }

}