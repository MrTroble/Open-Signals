package com.troblecodings.signals.guis;

import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.core.BlockPos;

public class PathwayRequesterContainer extends ContainerBase {

    protected PathwayRequesterTileEntity tile;
    protected Point start = null;
    protected Point end = null;
    protected BlockPos linkedPos;

    public PathwayRequesterContainer(GuiInfo info) {
        super(info);
        if (info.pos != null)
            this.tile = info.getTile();

    }

    @Override
    public void sendAllDataToRemote() {
        final WriteBuffer buffer = new WriteBuffer();
        final BlockPos signalBoxPos = tile.getLinkedSignalBox();
        buffer.putBlockPos(signalBoxPos == null ? BlockPos.ZERO : signalBoxPos);
        final Map.Entry<Point, Point> previousPathway = tile.getNextPathway();
        previousPathway.getKey().writeNetwork(buffer);
        previousPathway.getValue().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(ReadBuffer buffer) {
        this.linkedPos = buffer.getBlockPos();
        if (this.linkedPos.equals(BlockPos.ZERO))
            this.linkedPos = null;
        start = Point.of(buffer);
        end = Point.of(buffer);
        update();
    }

    @Override
    public void deserializeServer(ReadBuffer buffer) {
        final Point start = Point.of(buffer);
        final Point end = Point.of(buffer);
        tile.setNextPathway(start, end);
    }

}