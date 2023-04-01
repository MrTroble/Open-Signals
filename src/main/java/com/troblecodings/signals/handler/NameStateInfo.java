package com.troblecodings.signals.handler;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class NameStateInfo {

    public final Level world;
    public final BlockPos pos;

    public NameStateInfo(final Level world, final BlockPos pos) {
        this.world = world;
        this.pos = pos;
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
        final NameStateInfo other = (NameStateInfo) obj;
        return Objects.equals(pos, other.pos) && Objects.equals(world, other.world);
    }

}
