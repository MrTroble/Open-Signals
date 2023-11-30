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
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class RedstoneIOTileEntity extends SyncableTileEntity implements ISyncable {

    public RedstoneIOTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private final List<BlockPos> linkedSignalController = new ArrayList<>();
    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";
    public static final String LINKED_SIGNAL_CONTROLLER = "linkedSignalContrller";

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty()
                ? this.getBlockState().getBlock().getRegistryName().getPath()
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
        if (level.isClientSide)
            return;
        final boolean power = this.level.getBlockState(this.worldPosition)
                .getValue(RedstoneIO.POWER);
        final RedstoneUpdatePacket update = new RedstoneUpdatePacket(level, worldPosition, power,
                (RedstoneInput) this.getBlockState().getBlock());
        linkedPositions
        .forEach(pos -> loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerWorld) level,
                pos, (tile, _u) -> tile.getSignalBoxGrid().updateInput(update)));
linkedSignalController.forEach(pos -> loadChunkAndGetTile(SignalControllerTileEntity.class,
        (ServerWorld) level, pos, (tile, _u) -> tile.updateFromRSInput()));
    }

    public List<BlockPos> getLinkedController() {
        return ImmutableList.copyOf(linkedSignalController);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide)
            return;
        final LinkingUpdates update = SignalBoxHandler
                .getPosUpdates(new StateInfo(level, worldPosition));
        if (update == null)
            return;
        update.getPosToRemove().forEach(pos -> unlink(pos));
        update.getPosToAdd().forEach(pos -> link(pos));
        if (SignalBoxHandler.containsOutputUpdates(new StateInfo(level, worldPosition))) {
            BlockState state = level.getBlockState(worldPosition);
            state = state.setValue(RedstoneIO.POWER,
                    SignalBoxHandler.getNewOutputState(new StateInfo(level, worldPosition)));
            level.setBlockAndUpdate(worldPosition, state);
        }
    }

    public void link(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
    }

    public void unlink(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
    }

    public void linkController(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (!linkedSignalController.contains(pos))
            linkedSignalController.add(pos);
    }

    public void unlinkController(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (linkedSignalController.contains(pos))
            linkedSignalController.remove(pos);
    }

    @Override
    public boolean isValid(final PlayerEntity player) {
        // TODO Auto-generated method stub
        return false;
    }
}
