package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;

import net.minecraft.core.BlockPos;

public final class BlockposEntry extends IPathEntry<BlockPos> {

    private BlockPos position = BlockPos.ZERO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTWrapper tag) {
        tag.putBlockPos(this.getName(), position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTWrapper tag) {
        this.position = tag.getBlockPos(this.getName());
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
