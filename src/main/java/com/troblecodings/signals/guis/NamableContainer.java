package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.core.BlockPos;

public class NamableContainer extends ContainerBase implements INetworkSync {

    public BasicBlockEntity tile;
    private final GuiInfo info;
    protected BlockPos pos;
    protected List<BlockPos> linkedPos = new ArrayList<>();

    public NamableContainer(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.containerMenu = this;
        if (info.pos != null)
            this.tile = info.getTile();
        this.info = info;
    }

    private void sendSignalPos() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBlockPos(info.pos);
        buffer.putByte((byte) tile.getLinkedPos().size());
        tile.getLinkedPos().forEach(pos -> buffer.putBlockPos(pos));
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    @Override
    public void sendAllDataToRemote() {
        sendSignalPos();
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        pos = buffer.getBlockPos();
        final int size = buffer.getByteAsInt();
        for (int i = 0; i < size; i++)
            linkedPos.add(buffer.getBlockPos());
        tile = (BasicBlockEntity) info.world.getBlockEntity(pos);
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        final int byteLength = buffer.getByteAsInt();
        final byte[] array = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            array[i] = buffer.getByte();
        }
        final NameStateInfo info = new NameStateInfo(this.info.world, this.info.pos);
        final String name = new String(array);
        if (tile instanceof SignalTileEntity) {
            NameHandler.setNameForSignal(info, name);
        } else {
            NameHandler.setNameForNonSignal(info, name);
        }
    }
}