package com.troblecodings.signals.guis;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.util.math.BlockPos;

public class ContainerMonitor extends ContainerBase {

    private BlockPos pos;
    protected SignalBoxGrid grid = new SignalBoxGrid();
    protected MonitorTileEntity tile;
    protected int monitorSizeX, monitorSizeY;
    protected Point renderStart = new Point(-1, -1), renderEnd = new Point(-1, -1);
    protected boolean enableInteraction = false;

    private static final byte SEND_RENDER_POINTS = 0;
    private static final byte SEND_INTERACTION_ENABLE = 1;

    public ContainerMonitor(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        tile = (MonitorTileEntity) info.world.getTileEntity(info.pos);

        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBoolean(tile.hasLink());
        buffer.putInt(tile.getMonitorSizeX());
        buffer.putInt(tile.getMonitorSizeY());
        tile.getRenderStart().writeNetwork(buffer);
        tile.getRenderEnd().writeNetwork(buffer);
        buffer.putBlockPos(info.pos);
        buffer.putBoolean(tile.isEnableInteraction());
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final byte mode = buf.getByte();
        if (mode == SEND_INTERACTION_ENABLE) {
            tile.setEnableInteraction(buf.getBoolean());
        } else {
            final Point start = Point.of(buf);
            final Point end = Point.of(buf);
            tile.setRenderPoints(start, end);
        }
        MonitorNetworkHandler.sendTileData(tile, info.world.playerEntities);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        if (!buf.getBoolean()) {
            grid = null;
            update();
            return;
        }
        this.monitorSizeX = buf.getInt();
        this.monitorSizeY = buf.getInt();
        this.renderStart = Point.of(buf);
        this.renderEnd = Point.of(buf);
        this.pos = buf.getBlockPos();
        this.enableInteraction = buf.getBoolean();

        final MonitorTileEntity tile = (MonitorTileEntity) info.world.getTileEntity(pos);
        grid = tile.getGrid();
        update();
    }

    public void sendNewPointsToServer() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(SEND_RENDER_POINTS);
        renderStart.writeNetwork(buffer);
        renderEnd.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    public void sendInteractionEnable() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(SEND_INTERACTION_ENABLE);
        buffer.putBoolean(enableInteraction);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

}
