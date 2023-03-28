package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;

public class PosUpdateComponent {

    private final List<BlockPos> posToAdd;
    private final List<BlockPos> posToRemove;

    public PosUpdateComponent() {
        this.posToAdd = new ArrayList<>();
        this.posToRemove = new ArrayList<>();
    }

    public void addPosToLink(final BlockPos pos) {
        if (posToAdd.contains(pos))
            return;
        posToAdd.add(pos);
        if (posToRemove.contains(pos))
            posToRemove.remove(pos);
    }

    public void addPosToUnlink(final BlockPos pos) {
        if (posToRemove.contains(pos))
            return;
        posToRemove.add(pos);
        if (posToAdd.contains(pos))
            posToAdd.remove(pos);
    }

    public List<BlockPos> getPosToAdd() {
        return ImmutableList.copyOf(posToAdd);
    }

    public List<BlockPos> getPosToRemove() {
        return ImmutableList.copyOf(posToRemove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posToAdd, posToRemove);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PosUpdateComponent other = (PosUpdateComponent) obj;
        return Objects.equals(posToAdd, other.posToAdd)
                && Objects.equals(posToRemove, other.posToRemove);
    }
}