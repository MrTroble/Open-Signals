package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;

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

public class BasicBlockEntity extends TileEntity implements NamableWrapper {

    public static final String GUI_TAG = "guiTag";
    public static final String POS_TAG = "posTag";
    protected final ArrayList<BlockPos> linkedPositions = new ArrayList<>();
    protected String customName = null;

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
    public CompoundNBT serializeNBT() {
        final NBTWrapper wrapper = new NBTWrapper(super.serializeNBT());
        this.loadWrapper(wrapper);
        return wrapper.tag;
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
        if (customName == null)
            getNameWrapper();
        return customName != null;
    }

    public void setCustomName(final String name) {
        this.customName = name;
        final BlockState state = this.getBlockState();
        this.level.setBlocksDirty(worldPosition, state, state);
    }
}