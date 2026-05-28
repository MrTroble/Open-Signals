package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

    private int resetDelay = 0;
    private int blockingDelay = 0;

    private final List<BlockPos> linkedSignalController = new ArrayList<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    public static final String LINKED_SIGNAL_CONTROLLER = "linkedSignalContrller";
    public static final String RESET_DELAY = "resetDelay";
    public static final String BLOCKING_DELAY = "blockingDelay";

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty()
                ? this.getBlockType().getRegistryName().getResourcePath()
                : name;
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_LIST,
                linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper).toList());
        wrapper.putList(LINKED_SIGNAL_CONTROLLER,
                linkedSignalController.stream().map(NBTWrapper::getBlockPosWrapper).toList());
        wrapper.putInteger(RESET_DELAY, resetDelay);
        wrapper.putInteger(BLOCKING_DELAY, blockingDelay);
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        linkedSignalController.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
        wrapper.getList(LINKED_SIGNAL_CONTROLLER).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedSignalController::add);
        resetDelay = wrapper.getInteger(RESET_DELAY);
        blockingDelay = wrapper.getInteger(BLOCKING_DELAY);
    }

    private ScheduledFuture<?> resetTask;
    private final Consumer<RedstoneUpdatePacket> updateSignalBoxes = (packet) -> linkedPositions
            .forEach(pos -> loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) level, pos,
                    (tile, _u) -> tile.getSignalBoxGrid().updateInput(packet)));

    private void sendInputChanged(final RedstoneUpdatePacket packet) {
        updateSignalBoxes.accept(packet);
        linkedSignalController.forEach(pos -> loadChunkAndGetTile(SignalControllerTileEntity.class,
                world, pos, (tile, _u) -> tile.updateFromRSInput()));

    }

    public void sendInputOn() {
        if (resetTask != null) {
            resetTask.cancel(false);
            resetTask = null;
        }
        final RedstoneUpdatePacket packet = getRedstoneUpdatePacket();
        if (packet == null)
            return;
        if (blockingDelay > 0) {
            executor.schedule(() -> sendInputChanged(packet), blockingDelay, TimeUnit.SECONDS);
        } else {
            sendInputChanged(packet);
        }
    }

    public void sendInputOff() {
        final RedstoneUpdatePacket packet = getRedstoneUpdatePacket();
        if (packet == null)
            return;
        if (resetDelay <= 0) {
            updateSignalBoxes.accept(packet);
        } else {
            if (resetTask != null) {
                if (!resetTask.cancel(false))
                    return;
            }
            resetTask = executor.schedule(() -> {
                updateSignalBoxes.accept(packet);
                resetTask = null;
            }, resetDelay, TimeUnit.SECONDS);
        }
    }

    private RedstoneUpdatePacket getRedstoneUpdatePacket() {
        if (level.isClientSide)
            return null;
        final boolean power =
                this.level.getBlockState(this.worldPosition).getValue(RedstoneIO.POWER);
        return new RedstoneUpdatePacket(level, worldPosition, power,
                (RedstoneInput) this.getBlockState().getBlock());
    }

    public List<BlockPos> getLinkedController() {
        return ImmutableList.copyOf(linkedSignalController);
    }

    public int getResetDelay() {
        return resetDelay;
    }

    public void setResetDelay(final int resetDelay) {
        this.resetDelay = resetDelay;
    }

    public int getBlockingDelay() {
        return blockingDelay;
    }

    public void setBlockingDelay(final int blockingDelay) {
        this.blockingDelay = blockingDelay;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world == null || world.isRemote)
            return;
        final LinkingUpdates update =
                SignalBoxHandler.getPosUpdates(new StateInfo(level, worldPosition));
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
        if (!linkedPositions.contains(pos)) {
            linkedPositions.add(pos);
        }
    }

    public void unlink(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedPositions.contains(pos)) {
            linkedPositions.remove(pos);
        }
    }

    public void linkController(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (!linkedSignalController.contains(pos)) {
            linkedSignalController.add(pos);
        }
    }

    public void unlinkController(final BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedSignalController.contains(pos)) {
            linkedSignalController.remove(pos);
        }
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
