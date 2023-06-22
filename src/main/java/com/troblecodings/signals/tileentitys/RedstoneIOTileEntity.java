package com.troblecodings.signals.tileentitys;

import java.util.stream.Collectors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.handler.SignalBoxHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneIOTileEntity extends SyncableTileEntity implements ISyncable {

    public RedstoneIOTileEntity() {
        super();
    }

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty()
                ? this.getBlockType().getRegistryName().getResourcePath()
                : name;
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_LIST, linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper)
                .collect(Collectors.toList()));
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
    }

    public void sendToAll() {
        if (world.isRemote)
            return;
        final boolean power = this.world.getBlockState(this.pos).getValue(RedstoneIO.POWER);
        final RedstoneUpdatePacket update = new RedstoneUpdatePacket(world, pos, power,
                (RedstoneInput) getBlockType());
        linkedPositions.forEach(
                pos -> SignalBoxHandler.updateInput(new PosIdentifier(pos, world), update));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world == null || world.isRemote)
            return;
        final LinkingUpdates update = SignalBoxHandler.getPosUpdates(new PosIdentifier(pos, world));
        if (update == null)
            return;
        update.getPosToRemove().forEach(pos -> unlink(pos));
        update.getPosToAdd().forEach(pos -> link(pos));
        if (SignalBoxHandler.containsOutputUpdates(new PosIdentifier(pos, world))) {
            IBlockState state = world.getBlockState(pos);
            state = state.withProperty(RedstoneIO.POWER,
                    SignalBoxHandler.getNewOutputState(new PosIdentifier(pos, world)));
            world.setBlockState(pos, state);
        }
    }

    public void link(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
        this.syncClient();
    }

    public void unlink(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
        this.syncClient();
    }

    @Override
    public boolean isValid(final EntityPlayer player) {
        return true;
    }
    
    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState,
            final IBlockState newSate) {
        return false;
    }
}
