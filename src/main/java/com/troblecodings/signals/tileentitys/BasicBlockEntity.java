package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class BasicBlockEntity extends TileEntity implements NamableWrapper, IChunkLoadable {

    public static final String GUI_TAG = "guiTag";
    public static final String POS_TAG = "posTag";
    protected final ArrayList<BlockPos> linkedPositions = new ArrayList<>();

    public BasicBlockEntity(final TileEntityInfo info) {
        super(info.type);
    }

    public void saveWrapper(final NBTWrapper wrapper) {
    }

    public void loadWrapper(final NBTWrapper wrapper) {
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        saveWrapper(new NBTWrapper(nbt));
    }

    @Override
    public CompoundNBT save(final CompoundNBT nbt) {
        saveWrapper(new NBTWrapper(nbt));
        return super.save(nbt);
    }

    @Override
    public CompoundNBT serializeNBT() {
        final NBTWrapper wrapper = new NBTWrapper(super.serializeNBT());
        this.loadWrapper(wrapper);
        return wrapper.tag;
    }

    @Override
    public void load(final BlockState stae, final CompoundNBT nbt) {
        super.load(stae, nbt);
        loadWrapper(new NBTWrapper(nbt));
    }

    public List<BlockPos> getLinkedPos() {
        return ImmutableList.copyOf(linkedPositions);
    }

    @Override
    public String getNameWrapper() {
        final NameStateInfo info = new NameStateInfo(level, worldPosition);
        return level.isClientSide ? ClientNameHandler.getClientName(info)
                : NameHandler.getName(info);
    }

    @Override
    public boolean hasCustomName() {
        return !getNameWrapper().isEmpty();
    }
}