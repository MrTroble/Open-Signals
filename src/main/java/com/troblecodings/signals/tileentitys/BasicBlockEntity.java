package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BasicBlockEntity extends BlockEntity implements NamableWrapper, IChunkLoadable {

    public static final String GUI_TAG = "guiTag";
    public static final String POS_TAG = "posTag";
    protected final ArrayList<BlockPos> linkedPositions = new ArrayList<>();

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

    public List<BlockPos> getLinkedPos() {
        return ImmutableList.copyOf(linkedPositions);
    }

    @Override
    public String getNameWrapper() {
        final StateInfo info = new StateInfo(level, worldPosition);
        return level.isClientSide ? ClientNameHandler.getClientName(info)
                : NameHandler.getName(info);

    }

    @Override
    public boolean hasCustomName() {
        return !getNameWrapper().isEmpty();
    }
}