package com.troblecodings.signals.signalbox;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.LoadHolder;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.StateLoadHolder;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

    private final SignalBoxGrid grid;

    public SignalBoxTileEntity(final TileEntityInfo info) {
        super(info);
        grid = SignalBoxFactory.getFactory().getGrid();
    }

    @Override
    public void setLevelAndPosition(final World world, final BlockPos blockPos) {
        super.setLevelAndPosition(world, blockPos);
        grid.setTile(this);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        grid.writePathways(wrapper);
        SignalBoxHandler.writeTileNBT(new StateInfo(level, worldPosition), wrapper);
        wrapper.putWrapper(GUI_TAG, gridTag);
    }

    private NBTWrapper copy = null;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        grid.setTile(this);
        grid.read(wrapper.getWrapper(GUI_TAG));
        grid.readPathways(wrapper);
        copy = wrapper.copy();
        if (level != null) {
            onLoad();
        }
    }

    @Override
    public boolean hasLink() {
        return !SignalBoxHandler.isTileEmpty(new StateInfo(level, worldPosition));
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundNBT tag) {
        if (level.isClientSide)
            return false;
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(new ResourceLocation(OpenSignalsMain.MODID,
                tag.getString(OSItems.readStringFromPos(pos))));
        if (block == null || block instanceof AirBlock)
            return false;
        LinkType type = LinkType.SIGNAL;
        if (block == OSBlocks.REDSTONE_IN || block == OSBlocks.COMBI_REDSTONE_INPUT) {
            type = LinkType.INPUT;
        } else if (block == OSBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        } else if (block == OSBlocks.SIGNAL_BOX) {
            type = LinkType.SIGNALBOX;
        }
        if (type.equals(LinkType.SIGNAL)) {
            SignalStateHandler
                    .loadSignal(new StateLoadHolder(new SignalStateInfo(level, pos, (Signal) block),
                            new LoadHolder<>(new StateInfo(level, pos))));
        }
        return SignalBoxHandler.linkPosToSignalBox(new StateInfo(level, worldPosition), pos,
                (BasicBlock) block, type);
    }

    @Override
    public void onLoad() {
        grid.setTile(this);
        grid.onLoad();
        if (level.isClientSide) {
            return;
        }
        final StateInfo identifier = new StateInfo(level, worldPosition);
        SignalBoxHandler.putGrid(identifier, grid);
        SignalBoxHandler.readTileNBT(identifier, copy == null ? new NBTWrapper() : copy);
        SignalBoxHandler.loadSignals(identifier);
    }

    @Override
    public boolean unlink() {
        SignalBoxHandler.unlinkAll(new StateInfo(level, worldPosition));
        return true;
    }

    public boolean isBlocked() {
        return !this.clientSyncs.isEmpty();
    }

    @Override
    public boolean isValid(final PlayerEntity player) {
        if (clientSyncs.isEmpty())
            return false;
        return this.clientSyncs.get(0).getPlayer().equals(player);
    }

    public SignalBoxGrid getSignalBoxGrid() {
        return grid;
    }

    @Override
    public boolean canBeLinked() {
        return true;
    }
}
