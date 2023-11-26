package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.util.math.BlockPos;

public class PathwayRequesterContainer extends ContainerBase implements IChunkLoadable {

    protected final List<Point> validStarts = new ArrayList<>();
    protected final List<Point> validEnds = new ArrayList<>();
    protected PathwayRequesterTileEntity tile;
    protected Point start = null;
    protected Point end = null;
    protected BlockPos linkedPos;

    public PathwayRequesterContainer(final GuiInfo info) {
        super(info);
        this.tile = info.getTile();
    }

    @Override
    public void sendAllDataToRemote() {
        final WriteBuffer buffer = new WriteBuffer();
        final BlockPos signalBoxPos = tile.getLinkedSignalBox();
        buffer.putBlockPos(signalBoxPos == null ? BlockPos.ORIGIN : signalBoxPos);
        final Map.Entry<Point, Point> previousPathway = tile.getNextPathway();
        previousPathway.getKey().writeNetwork(buffer);
        previousPathway.getValue().writeNetwork(buffer);
        if (signalBoxPos != null) {
            final AtomicReference<SignalBoxGrid> grid = new AtomicReference<>();
            grid.set(SignalBoxHandler.getGrid(new StateInfo(info.world, signalBoxPos)));
            if (grid.get() == null)
                loadChunkAndGetTile(SignalBoxTileEntity.class, info.world, signalBoxPos,
                        (tile, _u) -> grid.set(tile.getSignalBoxGrid()));
            List<Point> validStarts = grid.get().getValidStarts();
            List<Point> validEnds = grid.get().getValidEnds();
            buffer.putByte((byte) validStarts.size());
            validStarts.forEach(point -> point.writeNetwork(buffer));
            buffer.putByte((byte) validEnds.size());
            validEnds.forEach(point -> point.writeNetwork(buffer));
        }
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        this.linkedPos = buffer.getBlockPos();
        if (this.linkedPos.equals(BlockPos.ORIGIN))
            this.linkedPos = null;
        start = Point.of(buffer);
        end = Point.of(buffer);

        validStarts.clear();
        validEnds.clear();
        if (linkedPos == null)
            return;
        final int validStartsSize = buffer.getByteToUnsignedInt();
        for (int i = 0; i < validStartsSize; i++) {
            validStarts.add(Point.of(buffer));
        }
        final int validEndsSize = buffer.getByteToUnsignedInt();
        for (int i = 0; i < validEndsSize; i++) {
            validEnds.add(Point.of(buffer));
        }
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final Point start = Point.of(buffer);
        final Point end = Point.of(buffer);
        tile.setNextPathway(start, end);
    }
}