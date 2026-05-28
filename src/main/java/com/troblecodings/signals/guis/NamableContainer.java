package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.util.math.BlockPos;

public class NamableContainer extends ContainerBase {

    protected BasicBlockEntity tile;
    protected BlockPos pos;
    protected int blockingDelay = 0;
    protected int resetDelay = 0;
    protected final List<BlockPos> linkedPos = new ArrayList<>();
    protected final List<BlockPos> linkedController = new ArrayList<>();

    public NamableContainer(final GuiInfo info) {
        super(info);
        if (info.pos != null) {
            this.tile = info.getTile();
        }
    }

    private void sendSignalPos() {
        final WriteBuffer buffer = new WriteBuffer();
        if (tile == null)
            tile = info.getTile(BasicBlockEntity.class);
        buffer.putBlockPos(info.pos);
        buffer.putList(tile.getLinkedPos(), WriteBuffer.BLOCKPOS_CONSUMER);
        if (tile instanceof RedstoneIOTileEntity) {
            final RedstoneIOTileEntity ioTile = (RedstoneIOTileEntity) tile;
            buffer.putList(ioTile.getLinkedController(), WriteBuffer.BLOCKPOS_CONSUMER);
            buffer.putInt(ioTile.getBlockingDelay());
            buffer.putInt(ioTile.getResetDelay());
        }
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    @Override
    public void sendAllDataToRemote() {
        sendSignalPos();
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        linkedPos.clear();
        linkedController.clear();
        pos = buffer.getBlockPos();
        tile = (BasicBlockEntity) info.world.getBlockEntity(pos);
        linkedPos.addAll(buffer.getList(ReadBuffer.BLOCKPOS_FUNCTION));
        if (tile instanceof RedstoneIOTileEntity) {
            linkedController.addAll(buffer.getList(ReadBuffer.BLOCKPOS_FUNCTION));
            blockingDelay = buffer.getInt();
            resetDelay = buffer.getInt();
        }
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final NamableContainerNetwork mode = buffer.getEnumValue(NamableContainerNetwork.class);
        if (mode.equals(NamableContainerNetwork.NAME)) {
            final StateInfo info = new StateInfo(this.info.world, this.info.pos);
            final String name = buffer.getString();
            if (tile instanceof SignalTileEntity) {
                NameHandler.setNameForSignal(info, name);
            } else {
                NameHandler.setNameForNonSignal(info, name);
            }
        }
        if (!(tile instanceof RedstoneIOTileEntity))
            return;
        final RedstoneIOTileEntity ioTile = (RedstoneIOTileEntity) tile;
        if (mode.equals(NamableContainerNetwork.BLOCKING_TIME)) {
            ioTile.setBlockingDelay(buffer.getInt());
        }
        if (mode.equals(NamableContainerNetwork.RESET_TIME)) {
            ioTile.setResetDelay(buffer.getInt());
        }
        tile.setChanged();
    }

    protected void sendNameToServer(final String name) {
        final WriteBuffer buffer = getBuffer(NamableContainerNetwork.NAME);
        buffer.putString(name);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendDelayTimeToServer(final int delay, final NamableContainerNetwork mode) {
        final WriteBuffer buffer = getBuffer(mode);
        buffer.putInt(delay);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private static WriteBuffer getBuffer(final NamableContainerNetwork mode) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(mode);
        return buffer;
    }

    protected static enum NamableContainerNetwork {

        NAME, RESET_TIME, BLOCKING_TIME;

    }
}