package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BasicBlockEntity extends BlockEntity {

    public static final String GUI_TAG = "guiTag";

    public BasicBlockEntity(final TileEntityInfo info) {
        super(info.type, info.pos, info.state);
    }

    public void saveWrapper(final NBTWrapper wrapper) {
    }

    public void loadWrapper(final NBTWrapper wrapper) {
    }

    @Override
    protected final void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        saveWrapper(new NBTWrapper(tag));
    }

    @Override
    public final void load(final CompoundTag tag) {
        this.loadWrapper(new NBTWrapper(tag));
        super.load(tag);
    }
}
