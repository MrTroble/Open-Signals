package com.troblecodings.signals.guis;

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
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.util.math.BlockPos;

public class ContainerPathwayRequester extends ContainerBase implements IChunkLoadable {

    protected SignalBoxGrid grid;
    protected PathwayRequesterTileEntity tile;
    protected Point start = null;
    protected Point end = null;
    protected BlockPos linkedPos;
    protected int addToPWToSavedPW;

    public ContainerPathwayRequester(final GuiInfo info) {
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
        buffer.putBoolean(tile.shouldPWBeAddedToSaver());
        if (signalBoxPos != null) {
            final AtomicReference<SignalBoxGrid> grid = new AtomicReference<>();
            grid.set(SignalBoxHandler.getGrid(new StateInfo(info.world, signalBoxPos)));
            if (grid.get() == null)
                loadChunkAndGetTile(SignalBoxTileEntity.class, info.world, signalBoxPos,
                        (tile, _u) -> grid.set(tile.getSignalBoxGrid()));
            grid.get().writeNetwork(buffer);
        }
        if (signalBoxPos != null)
            OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        this.linkedPos = buffer.getBlockPos();
        if (this.linkedPos.equals(BlockPos.ORIGIN))
            this.linkedPos = null;
        start = Point.of(buffer);
        end = Point.of(buffer);
        addToPWToSavedPW = buffer.getByte();

        grid = SignalBoxFactory.getFactory().getGrid();
        grid.readNetwork(buffer);
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final int mode = buffer.getByte();
        if (mode == 0) {
            final Point start = Point.of(buffer);
            final Point end = Point.of(buffer);
            tile.setNextPathway(start, end);
        } else if (mode == 1) {
            tile.setAddPWToSaver(buffer.getBoolean());
        }
    }
}