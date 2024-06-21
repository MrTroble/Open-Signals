package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.MainSignalIdentifier;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalBox extends ContainerBase implements UIClientSync, IChunkLoadable {

    protected final Map<Point, List<MainSignalIdentifier>> greenSignals = new HashMap<>();
    protected final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = new HashMap<>();
    protected final Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();
    protected final List<Map.Entry<Point, Point>> nextPathways = new ArrayList<>();
    protected final Map<BlockPos, List<Point>> validInConnections = new HashMap<>();
    protected SignalBoxGrid grid;
    private final Map<BlockPos, LinkType> propertiesForType = new HashMap<>();
    private SignalBoxTileEntity tile;
    private Consumer<String> infoUpdates;
    private Consumer<List<SignalBoxNode>> colorUpdates;
    private Consumer<List<Point>> signalUpdates;
    private Runnable counterUpdater;
    private Consumer<List<Point>> trainNumberUpdater;

    public ContainerSignalBox(final GuiInfo info) {
        super(info);
        this.tile = info.getTile(SignalBoxTileEntity.class);
    }

    @Override
    public void sendAllDataToRemote() {
        this.grid = tile.getSignalBoxGrid();
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(SignalBoxNetwork.SEND_GRID);
        buffer.putBlockPos(info.pos);
        grid.writeNetwork(buffer);
        final StateInfo identifier = new StateInfo(info.world, tile.getPos());
        final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = SignalBoxHandler
                .getPossibleSubsidiaries(identifier);
        final Map<BlockPos, LinkType> positions = SignalBoxHandler.getAllLinkedPos(identifier);
        buffer.putInt(possibleSubsidiaries.size());
        possibleSubsidiaries.forEach((pos, list) -> {
            buffer.putBlockPos(pos);
            buffer.putByte((byte) list.size());
            list.forEach(state -> buffer.putByte((byte) state.getID()));
        });
        buffer.putInt(positions.size());
        positions.forEach((pos, type) -> {
            buffer.putBlockPos(pos);
            buffer.putByte((byte) type.ordinal());
        });
        final List<Map.Entry<Point, Point>> nextPathways = grid.getNextPathways();
        buffer.putByte((byte) nextPathways.size());
        nextPathways.forEach(entry -> {
            entry.getKey().writeNetwork(buffer);
            entry.getValue().writeNetwork(buffer);
        });
        final Map<BlockPos, List<Point>> validInConnections = new HashMap<>();
        positions.entrySet().stream().filter(entry -> entry.getValue().equals(LinkType.SIGNALBOX))
                .forEach(entry -> {
                    final AtomicReference<SignalBoxGrid> grid = new AtomicReference<>();
                    grid.set(SignalBoxHandler.getGrid(new StateInfo(info.world, entry.getKey())));
                    if (grid.get() == null)
                        loadChunkAndGetTile(SignalBoxTileEntity.class, info.world, entry.getKey(),
                                (otherTile, _u) -> grid.set(otherTile.getSignalBoxGrid()));
                    if (grid.get() != null)
                        validInConnections.put(entry.getKey(), grid.get().getAllInConnections());
                });
        buffer.putByte((byte) validInConnections.size());
        validInConnections.forEach((pos, list) -> {
            buffer.putBlockPos(pos);
            buffer.putByte((byte) list.size());
            list.forEach(point -> point.writeNetwork(buffer));
        });
        final List<MainSignalIdentifier> greenSignals = grid.getGreenSignals();
        buffer.putInt(greenSignals.size());
        greenSignals.forEach(signal -> signal.writeNetwork(buffer));
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        final SignalBoxNetwork mode = buffer.getEnumValue(SignalBoxNetwork.class);
        switch (mode) {
            case SEND_GRID: {
                final BlockPos pos = buffer.getBlockPos();
                if (this.tile == null) {
                    this.tile = (SignalBoxTileEntity) info.world.getTileEntity(pos);
                }
                grid = tile.getSignalBoxGrid();
                grid.readNetwork(buffer);
                enabledSubsidiaryTypes.putAll(grid.getAllSubsidiaries());
                propertiesForType.clear();
                possibleSubsidiaries.clear();
                nextPathways.clear();
                validInConnections.clear();
                greenSignals.clear();
                final int signalSize = buffer.getInt();
                for (int i = 0; i < signalSize; i++) {
                    final BlockPos signalPos = buffer.getBlockPos();
                    propertiesForType.put(signalPos, LinkType.SIGNAL);
                    final List<SubsidiaryState> validSubsidiaries = new ArrayList<>();
                    final int listSize = buffer.getByteToUnsignedInt();
                    for (int j = 0; j < listSize; j++) {
                        validSubsidiaries
                                .add(SubsidiaryState.ALL_STATES.get(buffer.getByteToUnsignedInt()));
                    }
                    possibleSubsidiaries.put(signalPos, validSubsidiaries);
                }
                final int size = buffer.getInt();
                for (int i = 0; i < size; i++) {
                    final BlockPos blockPos = buffer.getBlockPos();
                    final LinkType type = LinkType.of(buffer);
                    propertiesForType.put(blockPos, type);
                }
                final int nextPathwaySize = buffer.getByteToUnsignedInt();
                for (int i = 0; i < nextPathwaySize; i++) {
                    final Point start = Point.of(buffer);
                    final Point end = Point.of(buffer);
                    nextPathways.add(Maps.immutableEntry(start, end));
                }
                final int validInConnectionsSize = buffer.getByteToUnsignedInt();
                for (int i = 0; i < validInConnectionsSize; i++) {
                    final BlockPos boxPos = buffer.getBlockPos();
                    final List<Point> points = new ArrayList<>();
                    final int listSize = buffer.getByteToUnsignedInt();
                    for (int j = 0; j < listSize; j++) {
                        points.add(Point.of(buffer));
                    }
                    validInConnections.put(boxPos, points);
                }
                final int greenSignalsSize = buffer.getInt();
                for (int i = 0; i < greenSignalsSize; i++) {
                    final MainSignalIdentifier identifier = MainSignalIdentifier.of(buffer);

                    final Map<ModeSet, SubsidiaryEntry> subsidiary = enabledSubsidiaryTypes
                            .getOrDefault(identifier.getPoint(), new HashMap<>());
                    final SubsidiaryEntry entry = subsidiary.get(identifier.getModeSet());
                    if (entry != null) {
                        identifier.state = SignalState
                                .combine(entry.enumValue.getSubsidiaryShowType());
                    }

                    final List<MainSignalIdentifier> greenSignals = this.greenSignals
                            .computeIfAbsent(identifier.getPoint(), _u -> new ArrayList<>());
                    greenSignals.add(identifier);
                }
                enabledSubsidiaryTypes.forEach((point, map) -> {
                    map.forEach((modeSet, subsidiary) -> {
                        final MainSignalIdentifier identifier = new MainSignalIdentifier(
                                new ModeIdentifier(point, modeSet),
                                grid.getNode(point).getOption(modeSet).get()
                                        .getEntry(PathEntryType.SIGNAL).get(),
                                SignalState.combine(subsidiary.enumValue.getSubsidiaryShowType()));
                        final List<MainSignalIdentifier> greenSignals = this.greenSignals
                                .computeIfAbsent(identifier.getPoint(), _u -> new ArrayList<>());
                        greenSignals.add(identifier);
                    });
                });
                update();
                break;
            }
            case SEND_PW_UPDATE: {
                colorUpdates.accept(grid.readUpdateNetwork(buffer, false));
                break;
            }
            case PW_REQUEST_RESPONSE: {
                final PathwayRequestResult result = buffer.getEnumValue(PathwayRequestResult.class);
                infoUpdates.accept(I18Wrapper.format("error." + result.getName()));
                break;
            }
            case ADDED_TO_SAVER: {
                final PathwayRequestResult result = buffer.getEnumValue(PathwayRequestResult.class);
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                nextPathways.add(Maps.immutableEntry(start, end));
                infoUpdates.accept(I18Wrapper.format("error." + result.getName()) + " - "
                        + I18Wrapper.format("info.pathwaysaver"));
                break;
            }
            case OUTPUT_UPDATE: {
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                final boolean state = buffer.getBoolean();
                final SignalBoxNode node = grid.getNode(point);
                if (state) {
                    node.addManuellOutput(modeSet);
                } else {
                    node.removeManuellOutput(modeSet);
                }
                break;
            }
            case REMOVE_SAVEDPW: {
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                nextPathways.remove(Maps.immutableEntry(start, end));
                break;
            }
            case SET_SIGNALS: {
                final List<Point> pointUpdates = new ArrayList<>();
                final int redSignalSize = buffer.getByteToUnsignedInt();
                for (int i = 0; i < redSignalSize; i++) {
                    final MainSignalIdentifier identifier = MainSignalIdentifier.of(buffer);
                    greenSignals.remove(identifier.getPoint());
                    pointUpdates.add(identifier.getPoint());
                    removeFromEnabledSubsidiaries(identifier);
                }
                final int greenSignalSize = buffer.getByteToUnsignedInt();
                for (int i = 0; i < greenSignalSize; i++) {
                    final MainSignalIdentifier modeIdentifier = MainSignalIdentifier.of(buffer);
                    final List<MainSignalIdentifier> greenSignals = this.greenSignals
                            .computeIfAbsent(modeIdentifier.getPoint(), _u -> new ArrayList<>());

                    final Map<ModeSet, SubsidiaryEntry> subsidiary = enabledSubsidiaryTypes
                            .getOrDefault(modeIdentifier.getPoint(), new HashMap<>());
                    final SubsidiaryEntry entry = subsidiary.get(modeIdentifier.getModeSet());
                    if (entry != null) {
                        modeIdentifier.state = SignalState
                                .combine(entry.enumValue.getSubsidiaryShowType());
                    }
                    if (!greenSignals.contains(modeIdentifier))
                        greenSignals.add(modeIdentifier);

                    pointUpdates.add(modeIdentifier.getPoint());
                }
                signalUpdates.accept(pointUpdates);
                break;
            }
            case SEND_COUNTER: {
                grid.setCurrentCounter(buffer.getInt());
                counterUpdater.run();
                break;
            }
            case SEND_TRAIN_NUMBER: {
                final List<Point> updates = new ArrayList<>();
                final int size = buffer.getInt();
                for (int i = 0; i < size; i++) {
                    final Point point = Point.of(buffer);
                    final TrainNumber number = TrainNumber.of(buffer);
                    grid.getNode(point).setTrainNumber(number);
                    updates.add(point);
                }
                trainNumberUpdater.accept(updates);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        if (grid == null)
            grid = tile.getSignalBoxGrid();
        final SignalBoxNetwork mode = buffer.getEnumValue(SignalBoxNetwork.class);
        switch (mode) {
            case SEND_INT_ENTRY: {
                deserializeEntry(buffer, buffer.getByteToUnsignedInt());
                break;
            }
            case REMOVE_ENTRY: {
                final Point point = Point.of(buffer);
                final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
                final Rotation rotation = deserializeRotation(buffer);
                final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES
                        .get(buffer.getByteToUnsignedInt());
                final ModeSet modeSet = new ModeSet(guiMode, rotation);
                grid.getNode(point).getOption(modeSet)
                        .ifPresent(entry -> entry.removeEntry(entryType));
                break;
            }
            case SEND_POS_ENTRY: {
                deserializeEntry(buffer, buffer.getBlockPos());
                break;
            }
            case SEND_ZS2_ENTRY: {
                deserializeEntry(buffer, buffer.getByte());
                break;
            }
            case REMOVE_POS: {
                final BlockPos pos = buffer.getBlockPos();
                SignalBoxHandler
                        .unlinkPosFromSignalBox(new StateInfo(tile.getWorld(), tile.getPos()), pos);
                break;
            }
            case RESET_PW: {
                final Point point = Point.of(buffer);
                final SignalBoxPathway pw = grid.getPathwayByStartPoint(point);
                final boolean isShuntingPath = pw != null ? pw.isShuntingPath() : false;
                if (grid.resetPathway(point) && !isShuntingPath) {
                    grid.countOne();
                    final WriteBuffer sucess = new WriteBuffer();
                    sucess.putEnumValue(SignalBoxNetwork.SEND_COUNTER);
                    sucess.putInt(grid.getCurrentCounter());
                    OpenSignalsMain.network.sendTo(info.player, sucess);
                }
                break;
            }
            case REQUEST_PW: {
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                final PathwayRequestResult request = grid.requestWay(start, end);
                if (!request.isPass()) {
                    if (request.canBeAddedToSaver() && grid.addNextPathway(start, end)) {
                        final WriteBuffer sucess = new WriteBuffer();
                        sucess.putEnumValue(SignalBoxNetwork.ADDED_TO_SAVER);
                        sucess.putEnumValue(request);
                        start.writeNetwork(sucess);
                        end.writeNetwork(sucess);
                        OpenSignalsMain.network.sendTo(info.player, sucess);
                        break;
                    }
                    final WriteBuffer error = new WriteBuffer();
                    error.putEnumValue(SignalBoxNetwork.PW_REQUEST_RESPONSE);
                    error.putEnumValue(request);
                    OpenSignalsMain.network.sendTo(info.player, error);
                }
                break;
            }
            case RESET_ALL_PW: {
                grid.resetAllPathways();
                break;
            }
            case SEND_CHANGED_MODES: {
                grid.readUpdateNetwork(buffer, true);
                break;
            }
            case REQUEST_SUBSIDIARY: {
                final SubsidiaryEntry entry = SubsidiaryEntry.of(buffer);
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                grid.updateSubsidiarySignal(point, modeSet, entry);
                break;
            }
            case UPDATE_RS_OUTPUT: {
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                final boolean state = buffer.getBoolean();
                final BlockPos pos = grid.updateManuellRSOutput(point, modeSet, state);
                if (pos != null) {
                    SignalBoxHandler.updateRedstoneOutput(new StateInfo(info.world, pos), state);
                    final WriteBuffer sucess = new WriteBuffer();
                    sucess.putEnumValue(SignalBoxNetwork.OUTPUT_UPDATE);
                    point.writeNetwork(sucess);
                    modeSet.writeNetwork(sucess);
                    sucess.putBoolean(state);
                    OpenSignalsMain.network.sendTo(info.player, sucess);
                }
                break;
            }
            case SET_AUTO_POINT: {
                final Point point = Point.of(buffer);
                final boolean state = buffer.getBoolean();
                final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
                node.setAutoPoint(state);
                grid.updatePathwayToAutomatic(point);
                break;
            }
            case SEND_NAME: {
                final Point point = Point.of(buffer);
                final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
                node.setCustomText(buffer.getString());
                break;
            }
            case SEND_BOOL_ENTRY: {
                deserializeEntry(buffer, buffer.getBoolean());
                break;
            }
            case REMOVE_SAVEDPW: {
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                grid.removeNextPathway(start, end);
                break;
            }
            case SEND_POINT_ENTRY: {
                deserializeEntry(buffer, Point.of(buffer));
                break;
            }
            case SEND_COUNTER: {
                grid.setCurrentCounter(buffer.getInt());
                break;
            }
            case SEND_TRAIN_NUMBER: {
                final Point point = Point.of(buffer);
                final TrainNumber number = TrainNumber.of(buffer);
                grid.updateTrainNumber(point, number);
                break;
            }
            case RESET_ALL_SIGNALS: {
                grid.resetAllSignals();
                break;
            }
            case SEND_POSIDENT_LIST: {
                final List<PosIdentifier> list = new ArrayList<>();
                final int size = buffer.getInt();
                for (int i = 0; i < size; i++) {
                    list.add(PosIdentifier.of(buffer));
                }
                deserializeEntry(buffer, list);
                break;
            }
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void deserializeEntry(final ReadBuffer buffer, final T type) {
        final Point point = Point.of(buffer);
        final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
        final Rotation rotation = deserializeRotation(buffer);
        final PathEntryType<T> entryType = (PathEntryType<T>) PathEntryType.ALL_ENTRIES
                .get(buffer.getByteToUnsignedInt());
        final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
        final ModeSet modeSet = new ModeSet(guiMode, rotation);
        final Optional<PathOptionEntry> option = node.getOption(modeSet);
        if (option.isPresent()) {
            option.get().setEntry(entryType, type);
        } else {
            node.addAndSetEntry(modeSet, entryType, type);
        }
    }

    private static Rotation deserializeRotation(final ReadBuffer buffer) {
        return Rotation.values()[buffer.getByteToUnsignedInt()];
    }

    private void removeFromEnabledSubsidiaries(final MainSignalIdentifier identifier) {
        final Map<ModeSet, SubsidiaryEntry> map = enabledSubsidiaryTypes.get(identifier.getPoint());
        if (map == null)
            return;
        map.remove(identifier.getModeSet());
        if (map.isEmpty())
            enabledSubsidiaryTypes.remove(identifier.getPoint());
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.tile != null)
            this.tile.remove(this);
    }

    @Override
    public EntityPlayer getPlayer() {
        return this.info.player;
    }

    public Map<BlockPos, LinkType> getPositionForTypes() {
        return new HashMap<>(propertiesForType);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.info.player == null) {
            this.info.player = playerIn;
            this.tile.add(this);
        }
        return true;
    }

    protected void setInfoConsumer(final Consumer<String> consumer) {
        this.infoUpdates = consumer;
    }

    protected void setColorUpdater(final Consumer<List<SignalBoxNode>> updater) {
        this.colorUpdates = updater;
    }

    protected void setSignalUpdater(final Consumer<List<Point>> updater) {
        this.signalUpdates = updater;
    }

    protected void setConuterUpdater(final Runnable run) {
        this.counterUpdater = run;
    }

    protected void setTrainNumberUpdater(final Consumer<List<Point>> updater) {
        this.trainNumberUpdater = updater;
    }
}
