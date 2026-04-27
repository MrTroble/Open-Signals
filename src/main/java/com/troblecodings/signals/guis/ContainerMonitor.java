package com.troblecodings.signals.guis;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerMonitor extends ContainerBase {

    private BlockPos pos;
    protected SignalBoxGrid grid = new SignalBoxGrid();
    protected MonitorTileEntity tile;
    protected int monitorSizeX, monitorSizeY;
    protected Point renderStart, renderEnd;

    public ContainerMonitor(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        tile = (MonitorTileEntity) info.world.getBlockEntity(info.pos);

        final WriteBuffer buffer = new WriteBuffer();
        final SignalBoxGrid grid = tile.getGrid();
        buffer.putBoolean(tile.hasLink());
        grid.writeNetwork(buffer);
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
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        if (!buf.getBoolean()) {
            grid = null;
            update();
            return;
        }
        grid.readNetwork(buf);
        this.monitorSizeX = buf.getInt();
        this.monitorSizeY = buf.getInt();
        this.renderStart = Point.of(buf);
        this.renderEnd = Point.of(buf);
        this.pos = buf.getBlockPos();
        update();
    }

    public void sendNewPointsToServer() {
        if (renderStart == null || renderEnd == null)
            return;
        final BlockEntity tile = info.world.getBlockEntity(pos);
        if (tile != null) {
            ((MonitorTileEntity) tile).loadRenderPoints(renderStart, renderEnd, grid);
        }
        final WriteBuffer buffer = new WriteBuffer();
        renderStart.writeNetwork(buffer);
        renderEnd.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

}
