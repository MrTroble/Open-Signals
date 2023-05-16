package com.troblecodings.signals.core;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class PosIdentifier {

    public final BlockPos pos;
    public final Level world;

    public PosIdentifier(final BlockPos pos, final Level world) {
        this.pos = pos;
        this.world = world;
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
        PosIdentifier other = (PosIdentifier) obj;
        return Objects.equals(pos, other.pos) && Objects.equals(world, other.world);
    }

    @Override
    public String toString() {
        return "pos: " + pos + " world: " + world;
    }
}