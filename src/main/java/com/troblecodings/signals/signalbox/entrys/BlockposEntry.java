package com.troblecodings.signals.signalbox.entrys;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.NbtUtils;

public final class BlockposEntry extends IPathEntry<BlockPos> {

    private BlockPos position = BlockPos.ZERO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final CompoundTag tag) {
        tag.put(this.getName(), NbtUtils.createPosTag(position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final CompoundTag tag) {
        final CompoundTag compound = (CompoundTag) tag.get(getName());
        if (compound != null)
            this.position = NbtUtils.getPosFromTag(compound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockPos getValue() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final BlockPos pPosition) {
        this.position = pPosition;
        this.isDirty = true;
    }

}
