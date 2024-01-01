package com.troblecodings.signals.core;

import java.util.Objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StateInfo {

    public final BlockPos pos;
    public final World world;

    public StateInfo(final World world, final BlockPos pos) {
        this.pos = pos;
        this.world = world;
    }
    
    public boolean isWorldNullOrClientSide() {
        return world == null || world.isClientSide;
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