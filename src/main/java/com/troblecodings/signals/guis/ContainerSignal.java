package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ContainerSignal extends ContainerBase implements INetworkSync {

    public SignalTileEntity signalTile;
    private Level world;
    private BlockPos pos;
    private final Player player;

    public ContainerSignal(final GuiInfo info) {
        super(info);
        this.pos = info.pos;
        info.base = this;
        this.world = info.world;
        info.player.containerMenu = this;
        this.player = info.player;
        if (pos != null)
            this.signalTile = info.getTile();
    }

    private void sendSignalPos() {
        final ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putInt(pos.getX());
        buffer.putInt(pos.getY());
        buffer.putInt(pos.getZ());
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void sendAllDataToRemote() {
        sendSignalPos();
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        signalTile = (SignalTileEntity) world.getBlockEntity(pos);
        update();
    }

    @Override
    public void deserializeServer(ByteBuffer buf) {
        // add System to send Name
    }
}