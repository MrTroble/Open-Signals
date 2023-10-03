package com.troblecodings.signals.signalbox;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

    private final SignalBoxGrid grid;

    public SignalBoxTileEntity(final TileEntityInfo info) {
        super(info);
        grid = SignalBoxFactory.getFactory().getGrid();
    }

    @Override
    public void setLevel(final Level world) {
        super.setLevel(world);
        grid.setPosAndWorld(worldPosition, world);
        if (world.isClientSide)
            return;
        SignalBoxHandler.setWorld(new PosIdentifier(worldPosition, world));
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        SignalBoxHandler.writeTileNBT(new PosIdentifier(worldPosition, level), wrapper);
        wrapper.putWrapper(GUI_TAG, gridTag);
    }

    private NBTWrapper copy = null;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        grid.read(wrapper.getWrapper(GUI_TAG));
        copy = wrapper.copy();
        if (level != null) {
            onLoad();
        }
    }

    @Override
    public boolean hasLink() {
        return SignalBoxHandler.isTileEmpty(new PosIdentifier(worldPosition, level));
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toShortString())));
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
            SignalStateHandler.loadSignal(new SignalStateInfo(level, pos, (Signal) block));
        }
        return SignalBoxHandler.linkPosToSignalBox(new PosIdentifier(worldPosition, level), pos,
                (BasicBlock) block, type);
    }

    @Override
    public void onLoad() {
        grid.setPosAndWorld(worldPosition, level);
        if (level.isClientSide) {
            return;
        }
        final PosIdentifier identifier = new PosIdentifier(worldPosition, level);
        SignalBoxHandler.updateModeGrid(identifier, grid);
        SignalBoxHandler.readTileNBT(identifier, copy == null ? new NBTWrapper() : copy);
        SignalBoxHandler.loadSignals(identifier);
    }

    @Override
    public boolean unlink() {
        SignalBoxHandler.unlinkAll(new PosIdentifier(worldPosition, level));
        return true;
    }

    public boolean isBlocked() {
        return !this.clientSyncs.isEmpty();
    }

    @Override
    public boolean isValid(final Player player) {
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
