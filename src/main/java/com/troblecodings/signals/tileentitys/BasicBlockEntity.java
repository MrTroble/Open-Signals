package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.ClientNameHandler;
import com.troblecodings.signals.handler.NameHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicBlockEntity extends TileEntity implements NamableWrapper, IChunkLoadable {

    public static final String GUI_TAG = "guiTag";
    public static final String POS_TAG = "posTag";
    protected final ArrayList<BlockPos> linkedPositions = new ArrayList<>();

    public BasicBlockEntity() {
        super();
    }

    public void saveWrapper(final NBTWrapper wrapper) {
    }

    public void loadWrapper(final NBTWrapper wrapper) {
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        saveWrapper(new NBTWrapper(nbt));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTWrapper wrapper = new NBTWrapper(super.serializeNBT());
        this.loadWrapper(wrapper);
        return wrapper.tag;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        loadWrapper(new NBTWrapper(compound));
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        saveWrapper(new NBTWrapper(compound));
        return super.writeToNBT(compound);
    }

    public List<BlockPos> getLinkedPos() {
        return ImmutableList.copyOf(linkedPositions);
    }

    public void syncClient() {
        syncClient(getWorld(), getPos());
    }

    public void syncClient(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
        world.markBlockRangeForRenderUpdate(pos, pos);
        markDirty();
    }

    @Override
    public String getNameWrapper() {
        final StateInfo info = new StateInfo(world, pos);
        final String customName = world.isRemote ? ClientNameHandler.getClientName(info)
                : NameHandler.getName(info);
        return customName;
    }

    @Override
    public boolean hasCustomName() {
        return !getNameWrapper().isEmpty();
    }
}