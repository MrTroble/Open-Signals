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
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
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
        grid.setPosAndWorld(worldPosition, world);
        if (world.isClientSide)
            return;
        SignalBoxHandler.setWorld(new PosIdentifier(blockPos, world));
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        SignalBoxHandler.writeTileNBT(new PosIdentifier(worldPosition, level), wrapper);
        wrapper.putWrapper(GUI_TAG, gridTag);
        System.out.println();
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
    public boolean link(final BlockPos pos, final CompoundNBT tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(new ResourceLocation(OpenSignalsMain.MODID,
                tag.getString(SignalControllerTileEntity.SIGNAL_NAME)));
        if (block == null || block instanceof AirBlock)
            return false;
        LinkType type = LinkType.SIGNAL;
        if (block == OSBlocks.REDSTONE_IN || block == OSBlocks.COMBI_REDSTONE_INPUT) {
            type = LinkType.INPUT;
        } else if (block == OSBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
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
        SignalBoxHandler.readTileNBT(new PosIdentifier(worldPosition, level),
                copy == null ? new NBTWrapper() : copy, grid.getModeGrid());
        SignalBoxHandler.loadSignals(new PosIdentifier(worldPosition, level));
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
    public boolean isValid(final PlayerEntity player) {
        if (clientSyncs.isEmpty())
            return false;
        return this.clientSyncs.get(0).getPlayer().equals(player);
    }

    public SignalBoxGrid getSignalBoxGrid() {
        return grid;
    }
}
