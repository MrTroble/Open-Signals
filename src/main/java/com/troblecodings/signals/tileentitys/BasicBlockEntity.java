package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBlockEntity extends BlockEntity implements NamableWrapper {

    public static final String GUI_TAG = "guiTag";
    public static final String POS_TAG = "posTag";
    protected final ArrayList<BlockPos> linkedPositions = new ArrayList<>();
    protected String customName = null;

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
        return linkedPositions;
    }

    @Override
    public String getNameWrapper() {
        final NameStateInfo info = new NameStateInfo(level, worldPosition);
        if (customName == null || customName.isEmpty())
            customName = level.isClientSide ? ClientNameHandler.getClientName(info)
                    : NameHandler.getName(info);
        return customName == null ? "" : customName;
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(final String name) {
        this.customName = name;
        final BlockState state = this.getBlockState();
        this.level.setBlocksDirty(worldPosition, state, state);
    }
}