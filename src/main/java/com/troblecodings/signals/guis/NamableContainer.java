package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static final Byte MODE_NAME = 0;
    private static final Byte MODE_DELAY = 1;
    private static final Byte MODE_DELAY_UNIT = 2;

    protected BasicBlockEntity tile;
    protected BlockPos pos;
    protected int resetDelay = 0;
    protected TimeUnit resetTimeUnit = TimeUnit.SECONDS;
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
            buffer.putList(((RedstoneIOTileEntity) tile).getLinkedController(),
                    WriteBuffer.BLOCKPOS_CONSUMER);
            buffer.putInt(((RedstoneIOTileEntity) tile).getResetDelay());
            buffer.putEnumValue(((RedstoneIOTileEntity) tile).getTimeUnit());
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
            resetDelay = buffer.getInt();
            resetTimeUnit = buffer.getEnumValue(TimeUnit.class);
        }
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final byte mode = buffer.getByte();
        if (mode == MODE_NAME) {
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
        if (mode == MODE_DELAY) {
            ((RedstoneIOTileEntity) tile).setResetDelay(buffer.getInt());
        } else if (mode == MODE_DELAY_UNIT) {
            ((RedstoneIOTileEntity) tile).setTimeUnit(buffer.getEnumValue(TimeUnit.class));
        }

    }

    protected void sendNameToServer(final String name) {
        final WriteBuffer buffer = getBuffer(MODE_NAME);
        buffer.putString(name);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendDelayTimeToServer(final int delay) {
        final WriteBuffer buffer = getBuffer(MODE_DELAY);
        buffer.putInt(delay);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    protected void sendDelayTimeUnitToServer(final TimeUnit unit) {
        final WriteBuffer buffer = getBuffer(MODE_DELAY_UNIT);
        buffer.putEnumValue(unit);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    private static WriteBuffer getBuffer(final byte mode) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(mode);
        return buffer;
    }
}