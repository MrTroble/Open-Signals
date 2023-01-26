package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;

import net.minecraft.core.BlockPos;

public class NamableContainer extends ContainerBase implements INetworkSync {

    public BasicBlockEntity tile;
    private final GuiInfo info;
    protected BlockPos pos;

    public NamableContainer(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.containerMenu = this;
        if (info.pos != null)
            this.tile = info.getTile();
        this.info = info;
    }

    private void sendSignalPos() {
        final ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(info.pos.getX());
        buffer.putInt(info.pos.getY());
        buffer.putInt(info.pos.getZ());
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void sendAllDataToRemote() {
        sendSignalPos();
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        tile = (BasicBlockEntity) info.world.getBlockEntity(pos);
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final int byteLength = Byte.toUnsignedInt(buf.get());
        final byte[] array = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            array[i] = buf.get();
        }
        NameHandler.setName(info.world, info.pos, new String(array));
    }
}