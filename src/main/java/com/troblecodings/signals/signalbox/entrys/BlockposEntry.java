package com.troblecodings.signals.signalbox.entrys;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTUtil;

public final class BlockposEntry extends IPathEntry<BlockPos> {

    private BlockPos position = BlockPos.ORIGIN;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final CompoundTag tag) {
        tag.put(this.getName(), NBTUtil.createPosTag(position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final CompoundTag tag) {
        final CompoundTag compound = (CompoundTag) tag.get(getName());
        if (compound != null)
            this.position = NBTUtil.getPosFromTag(compound);
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
