package com.troblecodings.signals.core;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RedstonePacket {

    public final Level world;
    public final BlockPos pos;
    public final boolean state;

    public RedstonePacket(final Level world, final BlockPos pos, final boolean state) {
        this.world = world;
        this.pos = pos;
        this.state = state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, state, world);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RedstonePacket other = (RedstonePacket) obj;
        return Objects.equals(pos, other.pos) && state == other.state
                && Objects.equals(world, other.world);
    }

}
