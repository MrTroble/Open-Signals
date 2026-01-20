package com.troblecodings.signals.network;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
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

public class SignalBoxNetworkHandler {

    private static final byte REMOVE = 0;
    private static final byte ADD = 1;

    private static final SignalBoxNetworkMode GRID = new SignalBoxNetworkMode(
            (b, n) -> n.readForGrid(b));

    private static final SignalBoxNetworkMode ENTRY = new SignalBoxNetworkMode(
            (b, n) -> n.readEntry(b));

    private static final SignalBoxNetworkMode NODE_SPECIAL_ENTRIES = new SignalBoxNetworkMode(
            (b, n) -> n.readNodeSpecialEntries(b));

    private static final SignalBoxNetworkMode PATHWAY = new SignalBoxNetworkMode(
            (b, n) -> n.readPathwayAction(b));

    private static final SignalBoxNetworkMode PATHWAY_SAVER = new SignalBoxNetworkMode(
            (b, n) -> n.readSavedPathway(b));

    private static final SignalBoxNetworkMode SUBSIDIARY = new SignalBoxNetworkMode(
            (b, n) -> n.readSubsidiary(b));

    private static final SignalBoxNetworkMode TRAINNUMBER = new SignalBoxNetworkMode(
            (b, n) -> n.readUpdateTrainNumber(b));

    private static final SignalBoxNetworkMode DEBUG_POINTS = new SignalBoxNetworkMode(
            (b, n) -> n.readDebugPoints(b));

    private ContainerSignalBox container = null;

    public void setUpNetwork(final ContainerSignalBox container) {
        this.container = container;
    }

    public void removeNetwork() {
        this.container = null;
    }

    private boolean containerConnected() {
        return container != null;
    }

    public ContainerSignalBox getContainer() {
        return container;
    }

    public void sendModeAdd(final ModeIdentifier ident) {
        if (!containerConnected())
            return;
        sendBuffer(getEntryBuffer(ident, EntryNetworkMode.MODE_ADD));
    }

    public void sendModeRemove(final ModeIdentifier ident) {
        if (!containerConnected())
            return;
        sendBuffer(getEntryBuffer(ident, EntryNetworkMode.MODE_REMOVE));
    }

    public void sendEntryAdd(final ModeIdentifier ident, final PathEntryType<?> entryType,
            final IPathEntry<?> entry) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getEntryBuffer(ident, EntryNetworkMode.ENTRY_ADD);
        buffer.putInt(entryType.getID());
        entry.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendEntryRemove(final ModeIdentifier ident, final PathEntryType<?> type) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getEntryBuffer(ident, EntryNetworkMode.ENTRY_REMOVE);
        buffer.putInt(type.getID());
        sendBuffer(buffer);
    }

    public void sendAll() {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.SEND_ALL);
        getGrid().writeNetwork(buffer);
        container.addAdditionalInitialisationData(buffer);
        sendBuffer(buffer);
    }

    public void sendCounter() {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.COUNTER);
        buffer.putInt(getGrid().getCurrentCounter());
        sendBuffer(buffer);
    }

    public void sendNodeLabel(final Point point, final String label) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getNodeBuffer(point, NodeNetworkMode.LABEL);
        buffer.putString(label);
        sendBuffer(buffer);
    }

    public void sendAutoPoint(final Point point, final boolean autoPoint) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getNodeBuffer(point, NodeNetworkMode.AUTO_POINT);
        buffer.putBoolean(autoPoint);
        sendBuffer(buffer);
    }

    public void sendRequestPathway(final Point p1, final Point p2, final PathType type) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.REQUEST);
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        buffer.putEnumValue(type);
        sendBuffer(buffer);
    }

    public void sendResetPathway(final Point p1) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.RESET);
        p1.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendRequestResponse(final PathwayRequestResult result) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getPathwayBuffer(PathwayNetworkMode.RESPONSE);
        buffer.putEnumValue(result.getMode());
        sendBuffer(buffer);
    }

    public void sendResetAllPathways() {
        if (!containerConnected())
            return;
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_PATHWAYS));
    }

    public void sendResetAllSignals() {
        if (!containerConnected())
            return;
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_SIGNALS));
    }

    public void sendAddSavedPathway(final Point start, final Point end, final PathType type,
            final PathwayRequestResult result) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putByte(ADD);
        buffer.putEnumValue(type);
        buffer.putEnumValue(result.getMode());
        sendBuffer(buffer);
    }

    public void sendRemoveSavedPathway(final Point start, final Point end) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putByte(REMOVE);
        sendBuffer(buffer);
    }

    public void sendRemovePos(final BlockPos pos) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.REMOVE_POS);
        buffer.putBlockPos(pos);
        sendBuffer(buffer);
    }

    public void sendSubsidiary(final ModeIdentifier ident, final SubsidiaryState entry,
            final boolean enable) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = SUBSIDIARY.getBuffer();
        ident.writeNetwork(buffer);
        entry.writeNetwork(buffer);
        buffer.putBoolean(enable);
        sendBuffer(buffer);
    }

    public void sendManuellOutputAdd(final Point point, final ModeSet mode) {
        sendManuellOutput(point, mode, NodeNetworkMode.MANUELL_OUTPUT_ADD);
    }

    public void sendManuellOutputRemove(final Point point, final ModeSet mode) {
        sendManuellOutput(point, mode, NodeNetworkMode.MANUELL_OUTPUT_REMOVE);
    }

    private void sendManuellOutput(final Point point, final ModeSet mode,
            final NodeNetworkMode network) {
        final WriteBuffer buffer = getNodeBuffer(point, network);
        mode.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void updateTrainNumber(final Point point, final TrainNumber number) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = TRAINNUMBER.getBuffer();
        point.writeNetwork(buffer);
        number.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendDebugPoints(final List<Point> points) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = DEBUG_POINTS.getBuffer();
        buffer.putISaveableList(points);
        sendBuffer(buffer);
    }

    public void sendUpdateSignalStates(final SignalBoxNode node) {
        if (!containerConnected())
            return;
        final WriteBuffer buffer = getNodeBuffer(node.getPoint(), NodeNetworkMode.SIGNAL_STATE);
        node.writeSignalStates(buffer);
        sendBuffer(buffer);
    }

    private void readEntry(final ReadBuffer buffer) {
        final EntryNetworkMode mode = buffer.getEnumValue(EntryNetworkMode.class);
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SignalBoxNode node = getGrid().getOrCreateNode(ident.point);
        if (mode.equals(EntryNetworkMode.MODE_ADD) || mode.equals(EntryNetworkMode.MODE_REMOVE)) {
            node.applyModeNetworkChanges(ident);
            return;
        }
        final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES.get(buffer.getInt());
        final Optional<PathOptionEntry> optionEntry = node.getOption(ident.mode);
        if (mode.equals(EntryNetworkMode.ENTRY_REMOVE)) {
            optionEntry.ifPresent(entry -> entry.removeEntryNoNetwork(entryType));
            container.handleNodeUpdate(node, entryType);
            return;
        }
        final IPathEntry<?> entry = entryType.newValue();
        entry.readNetwork(buffer);
        optionEntry.ifPresent(e -> e.addEntry(entryType, entry));
        container.handleNodeUpdate(node, entryType);
    }

    private void readForGrid(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final GridNetworkMode mode = buffer.getEnumValue(GridNetworkMode.class);
        if (mode.equals(GridNetworkMode.SEND_ALL)) {
            grid.readNetwork(buffer);
            container.readAdditionalInitialisationData(buffer);
        } else if (mode.equals(GridNetworkMode.COUNTER)) {
            grid.setCounterFromNetwork(buffer.getInt());
            container.handleCounterUpdate();
        } else {
            final BlockPos pos = buffer.getBlockPos();
            SignalBoxHandler.unlinkPosFromSignalBox(new StateInfo(container.getTile().getLevel(),
                    container.getTile().getBlockPos()), pos);
        }
    }

    private void readNodeSpecialEntries(final ReadBuffer buffer) {
        final NodeNetworkMode mode = buffer.getEnumValue(NodeNetworkMode.class);
        final Point point = Point.of(buffer);
        final SignalBoxGrid grid = getGrid();
        final SignalBoxNode node = grid.getNode(point);
        if (mode.equals(NodeNetworkMode.LABEL)) {
            node.setCustomText(buffer.getString());
        }
        if (mode.equals(NodeNetworkMode.AUTO_POINT)) {
            node.setAutoPointFromNetwork(buffer.getBoolean());
            grid.updatePathwayToAutomatic(point);
        }
        if (mode.equals(NodeNetworkMode.MANUELL_OUTPUT_ADD)
                || mode.equals(NodeNetworkMode.MANUELL_OUTPUT_REMOVE)) {
            handleManuellOutput(node, buffer.getINetworkSaveable(ModeSet.class), mode);
        }
        if (mode.equals(NodeNetworkMode.SIGNAL_STATE)) {
            node.readSignalStates(buffer);
            container.handleSignalStateUpdate(node);
        }
    }

    private void handleManuellOutput(final SignalBoxNode node, final ModeSet mode,
            final NodeNetworkMode network) {
        final boolean state = network.equals(NodeNetworkMode.MANUELL_OUTPUT_ADD) ? true : false;
        if (container.isClientSide()) {
            node.handleManuellEnabledOutputUpdate(mode, state);
        } else {
            getGrid().updateManuellRSOutput(node.getPoint(), mode, state);
        }
    }

    private void readPathwayAction(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final PathwayNetworkMode mode = buffer.getEnumValue(PathwayNetworkMode.class);
        if (mode.equals(PathwayNetworkMode.RESPONSE)) {
            container.handlePathwayRequestResponse(buffer.getEnumValue(PathwayRequestMode.class));
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
        if (!request.wasSuccesfull()) {
            final SignalBoxNode endNode = grid.getNode(p2);
            if (request.canBeAddedToSaver(type) && type.equals(PathType.NORMAL)
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
            grid.count();
        }
    }

    private void readSavedPathway(final ReadBuffer buffer) {
        final Point p1 = Point.of(buffer);
        final Point p2 = Point.of(buffer);
        final byte state = buffer.getByte();
        if (state == REMOVE) {
            container.handleRemoveSavedPathway(p1, p2);
            return;
        }
        final PathType type = buffer.getEnumValue(PathType.class);
        final PathwayRequestMode result = buffer.getEnumValue(PathwayRequestMode.class);
        container.handleAddSavedPathway(p1, p2, type, result);
    }

    private void readSubsidiary(final ReadBuffer buffer) {
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SubsidiaryState entry = SubsidiaryState.of(buffer);
        final boolean state = buffer.getBoolean();
        container.updateServerSubsidiary(ident, entry, state);
    }

    private void readUpdateTrainNumber(final ReadBuffer buffer) {
        final Point point = Point.of(buffer);
        final TrainNumber number = TrainNumber.of(buffer);
        getGrid().updateTrainNumber(point, number);
    }

    private void readDebugPoints(final ReadBuffer buffer) {
        container.handleDebugPoints(
                buffer.getList(ReadBuffer.getINetworkSaveableFunction(Point.class)));
    }

    private WriteBuffer getSavedPathwayBuffer(final Point p1, final Point p2) {
        final WriteBuffer buffer = PATHWAY_SAVER.getBuffer();
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        return buffer;
    }

    private WriteBuffer getNodeBuffer(final Point point, final NodeNetworkMode mode) {
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

    private WriteBuffer getEntryBuffer(final ModeIdentifier ident, final EntryNetworkMode mode) {
        final WriteBuffer buffer = ENTRY.getBuffer();
        buffer.putEnumValue(mode);
        ident.writeNetwork(buffer);
        return buffer;
    }

    protected SignalBoxGrid getGrid() {
        return container.getGrid();
    }

    public void desirializeBuffer(final ReadBuffer buffer) {
        final SignalBoxNetworkMode mode = SignalBoxNetworkMode.getModeFromBuffer(buffer);
        mode.executeRead(buffer, this);
    }

    private void sendBuffer(final WriteBuffer buffer) {
        if (!containerConnected())
            return;
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
        LABEL, AUTO_POINT, MANUELL_OUTPUT_ADD, MANUELL_OUTPUT_REMOVE, SIGNAL_STATE;
    }

    private static enum GridNetworkMode {
        SEND_ALL, COUNTER, REMOVE_POS;
    }

    private static enum EntryNetworkMode {
        MODE_ADD, MODE_REMOVE, ENTRY_ADD, ENTRY_REMOVE;
    }
}