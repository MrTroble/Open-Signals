package com.troblecodings.signals.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.math.BlockPos;

public class SignalBoxNetworkHandler {

    private static final byte REMOVE = 0;
    private static final byte ADD = 1;

    public static final SignalBoxNetworkMode GRID =
            new SignalBoxNetworkMode((b, n) -> n.readForGrid(b));

    public static final SignalBoxNetworkMode ENTRY =
            new SignalBoxNetworkMode((b, n) -> n.readEntry(b));

    public static final SignalBoxNetworkMode NODE_SPECIAL_ENTRIES =
            new SignalBoxNetworkMode((b, n) -> n.readNodeSpecialEntries(b));

    public static final SignalBoxNetworkMode PATHWAY =
            new SignalBoxNetworkMode((b, n) -> n.readPathwayAction(b));

    public static final SignalBoxNetworkMode PATHWAY_SAVER =
            new SignalBoxNetworkMode((b, n) -> n.readSavedPathway(b));

    public static final SignalBoxNetworkMode SUBSIDIARY =
            new SignalBoxNetworkMode((b, n) -> n.readSubsidiary(b));

    public static final SignalBoxNetworkMode TRAINNUMBER =
            new SignalBoxNetworkMode((b, n) -> n.readUpdateTrainNumber(b));

    public static final SignalBoxNetworkMode DEBUG_POINTS =
            new SignalBoxNetworkMode((b, n) -> n.readDebugPoints(b));

    protected SignalBoxNetworkReader reader;
    protected final SignalBoxGrid grid;
    protected final List<SignalBoxNetworkListener> listeners = new ArrayList<>();

    public SignalBoxNetworkHandler(final SignalBoxGrid grid) {
        this(null, grid);
    }

    public SignalBoxNetworkHandler(final SignalBoxNetworkReader reader, final SignalBoxGrid grid) {
        this.reader = reader;
        this.grid = grid;
    }

    public void addListener(final SignalBoxNetworkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(final SignalBoxNetworkListener listener) {
        listeners.remove(listener);
    }

    public List<SignalBoxNetworkListener> getListeners() {
        return ImmutableList.copyOf(listeners);
    }

    public void setUpNetworkReader(final SignalBoxNetworkReader reader) {
        this.reader = reader;
    }

    public void removeNetworkReader() {
        this.reader = null;
    }

    public void sendModeAdd(final ModeIdentifier ident) {
        sendBuffer(getEntryBuffer(ident, EntryNetworkMode.MODE_ADD));
    }

    public void sendModeRemove(final ModeIdentifier ident) {
        sendBuffer(getEntryBuffer(ident, EntryNetworkMode.MODE_REMOVE));
    }

    public void sendEntryAdd(final ModeIdentifier ident, final PathEntryType<?> entryType,
            final IPathEntry<?> entry) {
        final WriteBuffer buffer = getEntryBuffer(ident, EntryNetworkMode.ENTRY_ADD);
        buffer.putInt(entryType.getID());
        entry.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendEntryRemove(final ModeIdentifier ident, final PathEntryType<?> type) {
        final WriteBuffer buffer = getEntryBuffer(ident, EntryNetworkMode.ENTRY_REMOVE);
        buffer.putInt(type.getID());
        sendBuffer(buffer);
    }

    public void sendAll(final SignalBoxNetworkReader reader) {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.SEND_ALL);
        getGrid().writeNetwork(buffer);
        reader.addAdditionalInitialisationData(buffer);
        sendBuffer(buffer);
    }

    public void sendAllTo(final SignalBoxNetworkReader reader,
            final SignalBoxNetworkListener listener) {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.SEND_ALL);
        getGrid().writeNetwork(buffer);
        reader.addAdditionalInitialisationData(buffer);
        listener.consumer.accept(buffer);
    }

    public void sendCounter() {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.COUNTER);
        buffer.putInt(getGrid().getCurrentCounter());
        sendBuffer(buffer);
    }

    public void sendUIProfile(final UISignalBoxProfile profile) {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.UPDATE_UI_PROFILE);
        buffer.putInt(profile.getID());
        sendBuffer(buffer);
    }

    public void sendNodeLabel(final Point point, final String label) {
        final WriteBuffer buffer = getNodeBuffer(point, NodeNetworkMode.LABEL);
        buffer.putString(label);
        sendBuffer(buffer);
    }

    public void sendAutoPoint(final Point point, final boolean autoPoint) {
        final WriteBuffer buffer = getNodeBuffer(point, NodeNetworkMode.AUTO_POINT);
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
        buffer.putEnumValue(result.getMode());
        sendBuffer(buffer);
    }

    public void sendResetAllPathways() {
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_PATHWAYS));
    }

    public void sendResetAllSignals() {
        sendBuffer(getPathwayBuffer(PathwayNetworkMode.RESET_ALL_SIGNALS));
    }

    public void sendAddSavedPathway(final Point start, final Point end, final PathType type,
            final PathwayRequestResult result) {
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putByte(ADD);
        buffer.putEnumValue(type);
        buffer.putEnumValue(result.getMode());
        sendBuffer(buffer);
    }

    public void sendRemoveSavedPathway(final Point start, final Point end) {
        final WriteBuffer buffer = getSavedPathwayBuffer(start, end);
        buffer.putByte(REMOVE);
        sendBuffer(buffer);
    }

    public void sendRemovePos(final BlockPos pos) {
        final WriteBuffer buffer = getGridBuffer(GridNetworkMode.REMOVE_POS);
        buffer.putBlockPos(pos);
        sendBuffer(buffer);
    }

    public void sendSubsidiary(final ModeIdentifier ident, final SubsidiaryState entry,
            final boolean enable) {
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
        final WriteBuffer buffer = TRAINNUMBER.getBuffer();
        point.writeNetwork(buffer);
        number.writeNetwork(buffer);
        sendBuffer(buffer);
    }

    public void sendDebugPoints(final List<Point> points) {
        final WriteBuffer buffer = DEBUG_POINTS.getBuffer();
        buffer.putISaveableList(points);
        sendBuffer(buffer);
    }

    public void sendUpdateSignalStates(final SignalBoxNode node) {
        final WriteBuffer buffer = getNodeBuffer(node.getPoint(), NodeNetworkMode.SIGNAL_STATE);
        node.writeSignalStates(buffer);
        sendBuffer(buffer);
    }

    protected void readEntry(final ReadBuffer buffer) {
        final EntryNetworkMode mode = buffer.getEnumValue(EntryNetworkMode.class);
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SignalBoxNode node = getGrid().getOrCreateNode(ident.point);
        if (mode.equals(EntryNetworkMode.MODE_ADD) || mode.equals(EntryNetworkMode.MODE_REMOVE)) {
            node.applyModeNetworkChanges(ident.mode);
            node.post();
            reader.onNodeUpdate(node);
            return;
        }
        final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES.get(buffer.getInt());
        final PathOptionEntry optionEntry = node.getOrCreateOption(ident.mode);
        if (mode.equals(EntryNetworkMode.ENTRY_REMOVE)) {
            optionEntry.removeEntryNoNetwork(entryType);
            reader.onEntryUpdate(node, entryType);
            return;
        }
        final IPathEntry<?> entry = entryType.newValue();
        entry.readNetwork(buffer);
        optionEntry.addEntry(entryType, entry);
        reader.onEntryUpdate(node, entryType);
    }

    protected void readForGrid(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final GridNetworkMode mode = buffer.getEnumValue(GridNetworkMode.class);
        if (mode.equals(GridNetworkMode.SEND_ALL)) {
            grid.readNetwork(buffer);
            reader.readAdditionalInitialisationData(buffer);
        } else if (mode.equals(GridNetworkMode.COUNTER)) {
            grid.setCounterFromNetwork(buffer.getInt());
            reader.handleCounterUpdate();
        } else if (mode.equals(GridNetworkMode.UPDATE_UI_PROFILE)) {
            grid.setUIProfile(UISignalBoxProfile.UI_PROFILES.get(buffer.getInt()));
            reader.handleUIProfileUpdate();
        } else {
            final BlockPos pos = buffer.getBlockPos();
            SignalBoxHandler.unlinkPosFromSignalBox(reader.getStateInfo(), pos);
        }
    }

    protected void readNodeSpecialEntries(final ReadBuffer buffer) {
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
        }
        reader.onNodeUpdate(node);
    }

    protected void handleManuellOutput(final SignalBoxNode node, final ModeSet mode,
            final NodeNetworkMode network) {
        final boolean state = network.equals(NodeNetworkMode.MANUELL_OUTPUT_ADD) ? true : false;
        if (reader.isClientSide()) {
            node.handleManuellEnabledOutputUpdate(mode, state);
        } else {
            getGrid().updateManuellRSOutput(node.getPoint(), mode, state);
        }
    }

    protected void readPathwayAction(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final PathwayNetworkMode mode = buffer.getEnumValue(PathwayNetworkMode.class);
        if (mode.equals(PathwayNetworkMode.RESPONSE)) {
            reader.handlePathwayRequestResponse(buffer.getEnumValue(PathwayRequestMode.class));
            return;
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
            if (request.canBeAddedToSaver(type) && !endNode.containsOutConnection()
                    && grid.addNextPathway(p1, p2, type)) {
                sendAddSavedPathway(p1, p2, type, request);
                return;
            }
            sendRequestResponse(request);
        }
    }

    protected void resetPathway(final Point p1) {
        final SignalBoxGrid grid = getGrid();
        final SignalBoxPathway pw = grid.getPathwayByStartPoint(p1);
        final boolean isShuntingPath = pw != null ? pw.isShuntingPath() : false;
        if (grid.resetPathway(p1) && !isShuntingPath) {
            grid.count();
        }
    }

    protected void readSavedPathway(final ReadBuffer buffer) {
        final Point p1 = Point.of(buffer);
        final Point p2 = Point.of(buffer);
        final byte state = buffer.getByte();
        if (state == REMOVE) {
            getGrid().removeNextPathway(p1, p2);
            reader.handleRemoveSavedPathway(p1, p2);
            return;
        }
        final PathType type = buffer.getEnumValue(PathType.class);
        final PathwayRequestMode result = buffer.getEnumValue(PathwayRequestMode.class);
        reader.handleAddSavedPathway(p1, p2, type, result);
    }

    protected void readSubsidiary(final ReadBuffer buffer) {
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SubsidiaryState entry = SubsidiaryState.of(buffer);
        final boolean state = buffer.getBoolean();
        reader.updateServerSubsidiary(ident, entry, state);
    }

    protected void readUpdateTrainNumber(final ReadBuffer buffer) {
        final Point point = Point.of(buffer);
        final TrainNumber number = TrainNumber.of(buffer);
        getGrid().updateTrainNumber(point, number);
    }

    protected void readDebugPoints(final ReadBuffer buffer) {
        reader.handleDebugPoints(
                buffer.getList(ReadBuffer.getINetworkSaveableFunction(Point.class)));
    }

    protected static WriteBuffer getSavedPathwayBuffer(final Point p1, final Point p2) {
        final WriteBuffer buffer = PATHWAY_SAVER.getBuffer();
        p1.writeNetwork(buffer);
        p2.writeNetwork(buffer);
        return buffer;
    }

    protected static WriteBuffer getNodeBuffer(final Point point, final NodeNetworkMode mode) {
        final WriteBuffer buffer = NODE_SPECIAL_ENTRIES.getBuffer();
        buffer.putEnumValue(mode);
        point.writeNetwork(buffer);
        return buffer;
    }

    protected static WriteBuffer getGridBuffer(final GridNetworkMode mode) {
        final WriteBuffer buffer = GRID.getBuffer();
        buffer.putEnumValue(mode);
        return buffer;
    }

    protected static WriteBuffer getPathwayBuffer(final PathwayNetworkMode mode) {
        final WriteBuffer buffer = PATHWAY.getBuffer();
        buffer.putEnumValue(mode);
        return buffer;
    }

    protected static WriteBuffer getEntryBuffer(final ModeIdentifier ident,
            final EntryNetworkMode mode) {
        final WriteBuffer buffer = ENTRY.getBuffer();
        buffer.putEnumValue(mode);
        ident.writeNetwork(buffer);
        return buffer;
    }

    public SignalBoxGrid getGrid() {
        return grid;
    }

    public void desirializeBuffer(final ReadBuffer buffer) {
        desirializeBuffer(buffer, SignalBoxNetworkMode.NETWORK_ENTRIES);
    }

    public void desirializeBuffer(final ReadBuffer buffer,
            final List<SignalBoxNetworkMode> allowedModes) {
        if (reader == null)
            return;
        final SignalBoxNetworkMode mode = SignalBoxNetworkMode.getModeFromBuffer(buffer);
        if (allowedModes.contains(mode)) {
            mode.executeRead(buffer, this);
        }
    }

    protected void sendBuffer(final WriteBuffer buffer) {
        listeners.forEach(listener -> listener.consumer.accept(buffer));
    }

    @Override
    public int hashCode() {
        return Objects.hash(listeners, reader);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        SignalBoxNetworkHandler other = (SignalBoxNetworkHandler) obj;
        return Objects.equals(listeners, other.listeners) && Objects.equals(reader, other.reader);
    }

    protected static enum PathwayNetworkMode {
        REQUEST, RESET, RESPONSE, RESET_ALL_PATHWAYS, RESET_ALL_SIGNALS;
    }

    protected static enum NodeNetworkMode {
        LABEL, AUTO_POINT, MANUELL_OUTPUT_ADD, MANUELL_OUTPUT_REMOVE, SIGNAL_STATE;
    }

    protected static enum GridNetworkMode {
        SEND_ALL, COUNTER, REMOVE_POS, UPDATE_UI_PROFILE;
    }

    protected static enum EntryNetworkMode {
        MODE_ADD, MODE_REMOVE, ENTRY_ADD, ENTRY_REMOVE;
    }

    public static class SignalBoxNetworkListener {

        public final StateInfo info;
        public final Consumer<WriteBuffer> consumer;

        public SignalBoxNetworkListener(final StateInfo info,
                final Consumer<WriteBuffer> consumer) {
            this.info = info;
            this.consumer = consumer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumer, info);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if ((obj == null) || (getClass() != obj.getClass()))
                return false;
            SignalBoxNetworkListener other = (SignalBoxNetworkListener) obj;
            return Objects.equals(consumer, other.consumer) && Objects.equals(info, other.info);
        }

    }
}