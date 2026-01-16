package com.troblecodings.signals.network;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.guis.ContainerSignalBox;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.core.BlockPos;

public class SignalBoxNetworkHandler implements INetworkSync {

    private static final boolean ADD = true;
    private static final boolean REMOVE = false;

    private final SignalBoxNetworkMode GRID = new SignalBoxNetworkMode(this::readForGrid);

    private final SignalBoxNetworkMode ENTRY = new SignalBoxNetworkMode(this::readEntry);

    private final SignalBoxNetworkMode NODE_SPECIAL_ENTRIES = new SignalBoxNetworkMode(
            this::readNodeSpecialEntries);

    private final SignalBoxNetworkMode PATHWAY = new SignalBoxNetworkMode(this::readPathwayAction);

    private final SignalBoxNetworkMode PATHWAY_SAVER = new SignalBoxNetworkMode(
            this::readSavedPathway);

    private final SignalBoxNetworkMode SUBSIDIARY = new SignalBoxNetworkMode(this::readSubsidiary);

    private final SignalBoxNetworkMode TRAINNUMBER = new SignalBoxNetworkMode(
            this::updateTrainNumber);

    private final SignalBoxNetworkMode DEBUG_POINTS = new SignalBoxNetworkMode(
            this::readDebugPoints);

    private final ContainerSignalBox container;

    public SignalBoxNetworkHandler(final ContainerSignalBox container) {
        this.container = container;
    }

    public void sendAddEntry(final ModeIdentifier ident, final PathEntryType<?> entryType,
            final IPathEntry<?> entry) {
        final WriteBuffer buffer = getEntryBuffer(ident, entryType, ADD);
        entry.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendEntryRemove(final ModeIdentifier ident, final PathEntryType<?> type) {
        sendBuffer(getEntryBuffer(ident, type, REMOVE));
    }

    public void sendAll() {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.SEND_ALL);
        getGrid().writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendCounter() {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.COUNTER);
        buffer.putInt(getGrid().getCurrentCounter());
        sendBuffer(buffer);
    }

    public void sendNodeLabel(final Point point, final String label) {
        final WriteBuffer buffer = getPointBuffer(point, NodeNetworkMode.LABEL);
        buffer.putString(label);
        sendBuffer(buffer);
    }

    public void sendAutoPoint(final Point point, final boolean autoPoint) {
        final WriteBuffer buffer = getPointBuffer(point, NodeNetworkMode.AUTO_POINT);
        buffer.putBoolean(autoPoint);
        sendBuffer(buffer);
    }

    public void sendRequestPathway(final Point p1, final Point p2, final PathType type) {
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.REQUEST);
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        buffer.putEnumValue(type);
        sendBuffer(buffer);
    }

    public void sendResetPathway(final Point p1) {
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.RESET);
        p1.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendRequestResponse(final PathwayRequestResult result) {
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.RESPONSE);
        buffer.putEnumValue(result);
        sendBuffer(buffer);
    }

    public void sendResetAllPathway() {
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_PATHWAYS));
    }

    public void sendResetAllSignals() {
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_SIGNALS));
    }

    public void sendAddSavedPathway(final Point start, final Point end, final PathType type,
            final PathwayRequestResult result) {
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putBoolean(ADD);
        buffer.putEnumValue(type);
        buffer.putEnumValue(result);
        sendBuffer(buffer);
    }

    public void sendRemoveSavedPathway(final Point start, final Point end) {
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putBoolean(REMOVE);
        sendBuffer(buffer);
    }

    public void sendRemovePos(final BlockPos pos) {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.REMOVE_POS);
        buffer.putBlockPos(pos);
        sendBuffer(buffer);
    }

    public void sendSubsidiary(final ModeIdentifier ident, final SubsidiaryEntry entry) {
        final WriteBuffer buffer = SUBSIDIARY.getBuffer();
        ident.writeNetwork(buffer);
        entry.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendOutputUpdate(final Point point, final List<ModeSet> list) {
        final WriteBuffer buffer = getPointBuffer(point, NodeNetworkMode.MANUELL_OUTPUT);
        // TODO Send list of outputs
        sendBuffer(buffer);
    }

    public void updateTrainNumber(final Point point, final TrainNumber number) {
        final WriteBuffer buffer = TRAINNUMBER.getBuffer();
        point.writeNetwork(buffer);
        number.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendDebugPoints(final List<Point> points) {
        final WriteBuffer buffer = DEBUG_POINTS.getBuffer();
        // TODO write List on Buffer
        sendBuffer(buffer);
    }

    private void readEntry(final ReadBuffer buffer) {
        final boolean state = buffer.getBoolean();
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES.get(buffer.getInt());
        final SignalBoxNode node = getGrid().getNode(ident.point);
        final Optional<PathOptionEntry> optionEntry = node.getOption(ident.mode);
        if (state == REMOVE) {
            optionEntry.ifPresent(entry -> entry.removeEntry(entryType));
            return;
        }
        final IPathEntry<?> entry = entryType.newValue();
        entry.readNetwork(buffer);
        optionEntry.ifPresent(e -> e.addEntry(entryType, entry));
    }

    private void readForGrid(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final GridNetworkMode mode = buffer.getEnumValue(GridNetworkMode.class);
        if (mode.equals(GridNetworkMode.SEND_ALL)) {
            grid.readNetwork(buffer);
        } else if (mode.equals(GridNetworkMode.COUNTER)) {
            grid.setCurrentCounter(buffer.getInt());
        } else {
            final BlockPos pos = buffer.getBlockPos();
            SignalBoxHandler.unlinkPosFromSignalBox(
                    new StateInfo(container.tile.getLevel(), container.tile.getBlockPos()), pos);
        }
    }

    private void readNodeSpecialEntries(final ReadBuffer buffer) {
        final NodeNetworkMode mode = buffer.getEnumValue(NodeNetworkMode.class);
        final Point point = Point.of(buffer);
        final SignalBoxNode node = getGrid().getNode(point);
        if (mode.equals(NodeNetworkMode.LABEL)) {
            node.setCustomText(buffer.getString());
        } else if (mode.equals(NodeNetworkMode.AUTO_POINT)) {
            node.setAutoPoint(buffer.getBoolean());
            getGrid().updatePathwayToAutomatic(point);
        } else {
            // TODO Set List of manuellOutputs on Node
        }
    }

    private void readPathwayAction(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final PathwayNetworkMode mode = buffer.getEnumValue(PathwayNetworkMode.class);
        if (mode.equals(PathwayNetworkMode.RESPONSE)) {
            final PathwayRequestResult result = buffer.getEnumValue(PathwayRequestResult.class);
            // TODO Send to Container/GUI
        }
        if (mode.equals(PathwayNetworkMode.RESET_ALL_PATHWAYS)) {
            grid.resetAllPathways();
            return;
        }
        if (mode.equals(PathwayNetworkMode.RESET_ALL_SIGNALS)) {
            grid.resetAllSignals();
            return;
        }
        final Point p1 = Point.of(buffer);
        if (mode.equals(PathwayNetworkMode.RESET)) {
            resetPathway(p1);
            return;
        }
        final Point p2 = Point.of(buffer);
        final PathType type = buffer.getEnumValue(PathType.class);
        final PathwayRequestResult request = grid.requestWay(p1, p2, type);
        if (!request.isPass()) {
            final SignalBoxNode endNode = grid.getNode(p2);
            if (request.canBeAddedToSaver() && type.equals(PathType.NORMAL)
                    && !endNode.containsOutConnection() && grid.addNextPathway(p1, p2, type)) {
                sendAddSavedPathway(p1, p2, type, request);
                return;
            }
            sendRequestResponse(request);
        }
    }

    private void resetPathway(final Point p1) {
        final SignalBoxGrid grid = getGrid();
        final SignalBoxPathway pw = grid.getPathwayByStartPoint(p1);
        final boolean isShuntingPath = pw != null ? pw.isShuntingPath() : false;
        if (grid.resetPathway(p1) && !isShuntingPath) {
            grid.countOne();
            sendCounter();
        }
    }

    private void readSavedPathway(final ReadBuffer buffer) {
        final Point start = Point.of(buffer);
        final Point end = Point.of(buffer);
        final boolean state = buffer.getBoolean();
        if (state == REMOVE)
            // TODO Remove from Container
            return;
        final PathType type = buffer.getEnumValue(PathType.class);
        final PathwayRequestResult result = buffer.getEnumValue(PathwayRequestResult.class);
        // TODO set in Container
    }

    private void readSubsidiary(final ReadBuffer buffer) {
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SubsidiaryEntry entry = SubsidiaryEntry.of(buffer);
        // TODO Set of Subsidioary
    }

    private void updateTrainNumber(final ReadBuffer buffer) {
        final Point point = Point.of(buffer);
        final TrainNumber number = TrainNumber.of(buffer);
        // TODO Update trainnumber
    }

    private void readDebugPoints(final ReadBuffer buffer) {
        // TODO Read Debug List
    }

    private WriteBuffer getSavedPathwayBuffer(final Point p1, final Point p2) {
        final WriteBuffer buffer = PATHWAY_SAVER.getBuffer();
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        return buffer;
    }

    private WriteBuffer getPointBuffer(final Point point, final NodeNetworkMode mode) {
        final WriteBuffer buffer = NODE_SPECIAL_ENTRIES.getBuffer();
        buffer.putEnumValue(mode);
        point.writeNetwork(buffer);
        return buffer;
    }

    private WriteBuffer getGridBuffer(final GridNetworkMode mode) {
        final WriteBuffer buffer = GRID.getBuffer();
        buffer.putEnumValue(mode);
        return buffer;
    }

    private WriteBuffer getPathwayBuffer(final PathwayNetworkMode mode) {
        final WriteBuffer buffer = PATHWAY.getBuffer();
        buffer.putEnumValue(mode);
        return buffer;
    }

    private WriteBuffer getEntryBuffer(final ModeIdentifier ident, final PathEntryType<?> type,
            final boolean state) {
        final WriteBuffer buffer = ENTRY.getBuffer();
        buffer.putBoolean(state);
        ident.writeNetwork(buffer);
        buffer.putInt(type.getID());
        return buffer;
    }

    protected SignalBoxGrid getGrid() {
        return container.grid;
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        executeRead(buf);
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        executeRead(buf);
    }

    private void executeRead(final ReadBuffer buffer) {
        final SignalBoxNetworkMode mode = SignalBoxNetworkMode.getModeFromBuffer(buffer);
        mode.executeRead(buffer);
    }

    private void sendBuffer(final WriteBuffer buffer) {
        OpenSignalsMain.network.sendTo(container.getPlayer(), buffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        SignalBoxNetworkHandler other = (SignalBoxNetworkHandler) obj;
        return Objects.equals(container, other.container);
    }

    private static enum PathwayNetworkMode {
        REQUEST, RESET, RESPONSE, RESET_ALL_PATHWAYS, RESET_ALL_SIGNALS;
    }

    private static enum NodeNetworkMode {
        LABEL, AUTO_POINT, MANUELL_OUTPUT;
    }

    private static enum GridNetworkMode {
        SEND_ALL, COUNTER, REMOVE_POS;
    }
}