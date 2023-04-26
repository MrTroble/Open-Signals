package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;

import net.minecraft.core.BlockPos;

public class LinkingUpdates {

    private final List<BlockPos> posToAdd;
    private final List<BlockPos> posToRemove;

    public LinkingUpdates() {
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

    private static final String POS_ADD = "posAdd";
    private static final String POS_REMOVE = "posRemove";

    public void writeNBT(final NBTWrapper wrapper) {
        final List<NBTWrapper> addList = new ArrayList<>();
        final List<NBTWrapper> removeList = new ArrayList<>();
        posToAdd.forEach(pos -> addList.add(NBTWrapper.getBlockPosWrapper(pos)));
        posToRemove.forEach(pos -> removeList.add(NBTWrapper.getBlockPosWrapper(pos)));
        wrapper.putList(POS_ADD, addList);
        wrapper.putList(POS_REMOVE, removeList);
    }

    public void readNBT(final NBTWrapper wrapper) {
        posToAdd.clear();
        posToRemove.clear();
        wrapper.getList(POS_ADD).forEach(tag -> posToAdd.add(tag.getAsPos()));
        wrapper.getList(POS_REMOVE).forEach(tag -> posToRemove.add(tag.getAsPos()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(posToAdd, posToRemove);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LinkingUpdates other = (LinkingUpdates) obj;
        return Objects.equals(posToAdd, other.posToAdd)
                && Objects.equals(posToRemove, other.posToRemove);
    }

    @Override
    public String toString() {
        return "LinkingUpdates: PosToAdd: " + posToAdd + ", PosToRemove: " + posToRemove;
    }
}