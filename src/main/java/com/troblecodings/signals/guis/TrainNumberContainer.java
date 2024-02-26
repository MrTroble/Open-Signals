package com.troblecodings.signals.guis;

import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.IChunkLoadable;
import com.troblecodings.signals.tileentitys.TrainNumberTileEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class TrainNumberContainer extends ContainerBase implements IChunkLoadable {

    private TrainNumberTileEntity tile;
    protected Point setPoint;
    protected TrainNumber number = TrainNumber.DEFAULT;
    protected BlockPos linkedPos;
    protected SignalBoxGrid grid;

    public TrainNumberContainer(final GuiInfo info) {
        super(info);
        if (info.pos != null) {
            this.tile = info.getTile();
        }
    }

    @Override
    public void sendAllDataToRemote() {
        final WriteBuffer buffer = new WriteBuffer();
        final BlockPos linkedPos = tile.getLinkedSignalBox();
        buffer.putBlockPos(linkedPos == null ? BlockPos.ZERO : linkedPos);
        tile.getCurrentPoint().writeNetwork(buffer);
        tile.getTrainNumber().writeNetwork(buffer);
        final AtomicReference<SignalBoxGrid> grid = new AtomicReference<>(
                SignalBoxHandler.getGrid(new StateInfo(info.world, linkedPos)));
        if (grid.get() == null) {
            loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerWorld) info.world, linkedPos,
                    (boxTile, _u) -> grid.set(boxTile.getSignalBoxGrid()));
        }
        if (grid.get() == null)
            return;
        grid.get().writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        this.linkedPos = buf.getBlockPos();
        if (this.linkedPos.equals(BlockPos.ZERO)) {
            this.linkedPos = null;
        }
        this.setPoint = Point.of(buf);
        this.number = TrainNumber.of(buf);
        this.grid = SignalBoxFactory.getFactory().getGrid();
        grid.readNetwork(buf);
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final TrainNumberNetwork mode = buf.getEnumValue(TrainNumberNetwork.class);
        if (mode.equals(TrainNumberNetwork.SEND_NEW_POINT)) {
            tile.setNewPoint(Point.of(buf));
        } else if (mode.equals(TrainNumberNetwork.SET_TRAINNUMBER)) {
            tile.updateTrainNumberManually();
        } else if (mode.equals(TrainNumberNetwork.SEND_NEW_TRAINNUMBER)) {
            tile.setNewTrainNumber(TrainNumber.of(buf));
        }
    }

    public static enum TrainNumberNetwork {

        SEND_NEW_TRAINNUMBER, SEND_NEW_POINT, SET_TRAINNUMBER;

    }

}