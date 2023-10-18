package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneIOTileEntity extends SyncableTileEntity implements ISyncable {

    public RedstoneIOTileEntity() {
        super();
    }

    private final List<BlockPos> linkedSignalController = new ArrayList<>();

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    public static final String LINKED_SIGNAL_CONTROLLER = "linkedSignalContrller";

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
        wrapper.putList(LINKED_SIGNAL_CONTROLLER, linkedSignalController.stream()
                .map(NBTWrapper::getBlockPosWrapper).collect(Collectors.toList()));
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        linkedSignalController.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
        wrapper.getList(LINKED_SIGNAL_CONTROLLER).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedSignalController::add);
    }

    public void sendToAll() {
        if (world.isRemote)
            return;
        final boolean power = this.world.getBlockState(this.pos).getValue(RedstoneIO.POWER);
        final RedstoneUpdatePacket update = new RedstoneUpdatePacket(world, pos, power,
                (RedstoneInput) getBlockType());
        linkedPositions.forEach(pos -> loadChunkAndGetTile(SignalBoxTileEntity.class, world, pos,
                (_u1, _u2) -> SignalBoxHandler.updateInput(new StateInfo(world, pos), update)));
        linkedSignalController.forEach(pos -> {
            loadChunkAndGetTile(SignalControllerTileEntity.class, world, pos,
                    (tile, _u) -> tile.updateFromRSInput());
        });
    }

    public List<BlockPos> getLinkedController() {
        return ImmutableList.copyOf(linkedSignalController);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world == null || world.isRemote)
            return;
        final LinkingUpdates update = SignalBoxHandler.getPosUpdates(new StateInfo(world, pos));
        if (update == null)
            return;
        update.getPosToRemove().forEach(pos -> unlink(pos));
        update.getPosToAdd().forEach(pos -> link(pos));
        if (SignalBoxHandler.containsOutputUpdates(new StateInfo(world, pos))) {
            IBlockState state = world.getBlockState(pos);
            state = state.withProperty(RedstoneIO.POWER,
                    SignalBoxHandler.getNewOutputState(new StateInfo(world, pos)));
            world.setBlockState(pos, state);
        }
    }

    public void link(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
    }

    public void unlink(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
    }

    public void linkController(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (!linkedSignalController.contains(pos))
            linkedSignalController.add(pos);
    }

    public void unlinkController(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedSignalController.contains(pos))
            linkedSignalController.remove(pos);
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
