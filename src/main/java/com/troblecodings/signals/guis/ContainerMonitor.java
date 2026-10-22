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

import net.minecraft.core.BlockPos;

public class ContainerMonitor extends ContainerBase {

    private BlockPos pos;
    protected SignalBoxGrid grid = new SignalBoxGrid();
    protected MonitorTileEntity tile;
    protected int monitorSizeX, monitorSizeY;
    protected Point renderStart = new Point(-1, -1), renderEnd = new Point(-1, -1);

    public ContainerMonitor(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        tile = (MonitorTileEntity) info.world.getBlockEntity(info.pos);

        final WriteBuffer buffer = new WriteBuffer();
        buffer.putBoolean(tile.hasLink());
        buffer.putInt(tile.getMonitorSizeX());
        buffer.putInt(tile.getMonitorSizeY());
        tile.getRenderStart().writeNetwork(buffer);
        tile.getRenderEnd().writeNetwork(buffer);
        buffer.putBlockPos(info.pos);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final Point start = Point.of(buf);
        final Point end = Point.of(buf);
        tile.setRenderPoints(start, end);
        MonitorNetworkHandler.sendTileData(tile, info.world.players());
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

        final MonitorTileEntity tile = (MonitorTileEntity) info.world.getBlockEntity(pos);
        grid = tile.getGrid();
        update();
    }

    public void sendNewPointsToServer() {
        if (renderStart == null || renderEnd == null)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        renderStart.writeNetwork(buffer);
        renderEnd.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

}
