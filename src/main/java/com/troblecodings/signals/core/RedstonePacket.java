package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.blocks.RedstoneInput;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RedstonePacket {

    public final Level world;
    public final BlockPos pos;
    public final boolean state;
    public final RedstoneInput block;

    public RedstonePacket(final Level world, final BlockPos pos, final boolean state,
            final RedstoneInput block) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.block = block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, state, world);
    }

    @Override
    public boolean equals(final Object obj) {
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
