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
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

    private final SignalBoxGrid grid;

    public SignalBoxTileEntity() {
        grid = SignalBoxFactory.getFactory().getGrid();
    }

    @Override
    public void setWorld(final World worldIn) {
        super.setWorld(worldIn);
        grid.setPosAndWorld(pos, world);
        SignalBoxHandler.setWorld(new StateInfo(world, pos));
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        SignalBoxHandler.writeTileNBT(new StateInfo(world, pos), wrapper);
        wrapper.putWrapper(GUI_TAG, gridTag);
    }

    private NBTWrapper copy = null;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        grid.read(wrapper.getWrapper(GUI_TAG));
        copy = wrapper.copy();
        if (world != null) {
            onLoad();
        }
    }

    @Override
    public boolean hasLink() {
        return SignalBoxHandler.isTileEmpty(new StateInfo(world, pos));
    }

    @Override
    public boolean link(final BlockPos pos, final NBTTagCompound tag) {
        final Block block = Block.REGISTRY.getObject(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toString())));
        if (block == null || block instanceof BlockAir)
            return false;
        LinkType type = LinkType.SIGNAL;
        if (block == OSBlocks.REDSTONE_IN || block == OSBlocks.COMBI_REDSTONE_INPUT) {
            type = LinkType.INPUT;
        } else if (block == OSBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (type.equals(LinkType.SIGNAL) && !world.isRemote) {
            SignalStateHandler
                    .loadSignal(new StateLoadHolder(new SignalStateInfo(world, pos, (Signal) block),
                            new LoadHolder<>(new StateInfo(world, pos))));
        }
        return SignalBoxHandler.linkPosToSignalBox(new StateInfo(world, this.pos), pos,
                (BasicBlock) block, type);
    }

    @Override
    public void onLoad() {
        grid.setPosAndWorld(pos, world);
        if (world.isRemote)
            return;
        final StateInfo identifier = new StateInfo(world, pos);
        SignalBoxHandler.readTileNBT(identifier, copy == null ? new NBTWrapper() : copy,
                grid.getModeGrid());
        SignalBoxHandler.loadSignals(identifier);
        System.out.println("Loaded [" + identifier + "]!");
    }

    @Override
    public void onChunkUnload() {
        SignalBoxHandler.unloadSignals(new StateInfo(world, pos));
        System.out.println("Unloaded [" + new StateInfo(world, pos) + "]!");
    }

    @Override
    public boolean unlink() {
        SignalBoxHandler.unlinkAll(new StateInfo(world, pos));
        return true;
    }

    public boolean isBlocked() {
        return !this.clientSyncs.isEmpty();
    }

    @Override
    public boolean isValid(final EntityPlayer player) {
        if (clientSyncs.isEmpty())
            return false;
        return this.clientSyncs.get(0).getPlayer().equals(player);
    }

    public SignalBoxGrid getSignalBoxGrid() {
        return grid;
    }
}
