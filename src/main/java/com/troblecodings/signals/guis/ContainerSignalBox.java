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
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SubsidiarySignalParser;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerSignalBox extends ContainerBase implements UIClientSync, IChunkLoadable {

    protected final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = new HashMap<>();
    protected final Map<Point, Map<ModeSet, SubsidiaryState>> enabledSubsidiaryTypes = new HashMap<>();
    protected final Map<Map.Entry<Point, Point>, PathType> nextPathways = new HashMap<>();
    protected final Map<BlockPos, List<Point>> validInConnections = new HashMap<>();
    protected SignalBoxGrid grid;
    private EntityPlayer player;
    private final Map<BlockPos, LinkType> posForType = new HashMap<>();
    private SignalBoxTileEntity tile;
    private Consumer<String> infoUpdates;
    private Consumer<List<SignalBoxNode>> colorUpdates;
    private Runnable counterUpdater;
    private Consumer<List<SignalBoxNode>> trainNumberUpdater;
    private Consumer<List<Point>> debugPoints;

    protected Consumer<SignalBoxNode> updateSignalState = (node) -> {
    };

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
        final Map<BlockPos, LinkType> positions = SignalBoxHandler.getAllLinkedPos(identifier);
        final Map<Map.Entry<Point, Point>, PathType> nextPathways = grid.getNextPathways();
        buffer.putMap(positions, WriteBuffer.BLOCKPOS_CONSUMER, WriteBuffer.getEnumConsumer());
        buffer.putMap(nextPathways, (b, entry) -> {
            entry.getKey().writeNetwork(b);
            entry.getValue().writeNetwork(b);
        }, WriteBuffer.getEnumConsumer());
        final Map<BlockPos, List<Point>> validInConnections = new HashMap<>();
        positions.entrySet().stream().filter(entry -> entry.getValue().equals(LinkType.SIGNALBOX))
                .forEach(entry -> {
                    final AtomicReference<SignalBoxGrid> grid = new AtomicReference<>();
                    grid.set(SignalBoxHandler.getGrid(new StateInfo(info.world, entry.getKey())));
                    if (grid.get() == null) {
                        loadChunkAndGetTile(SignalBoxTileEntity.class, info.world, entry.getKey(),
                                (otherTile, _u) -> grid.set(otherTile.getSignalBoxGrid()));
                    }
                    if (grid.get() != null) {
                        validInConnections.put(entry.getKey(), grid.get().getAllInConnections());
                    }
                });
        buffer.putMap(validInConnections, WriteBuffer.BLOCKPOS_CONSUMER,
                (b, list) -> b.putISaveableList(list));
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
                posForType.clear();
                nextPathways.clear();
                validInConnections.clear();
                posForType.putAll(buffer.getMap(ReadBuffer.BLOCKPOS_FUNCTION,
                        ReadBuffer.getEnumFunction(LinkType.class)));
                nextPathways
                        .putAll(buffer.getMap((b -> Maps.immutableEntry(Point.of(b), Point.of(b))),
                                b -> b.getEnumValue(PathType.class)));
                validInConnections.putAll(buffer.getMap(ReadBuffer.BLOCKPOS_FUNCTION,
                        b -> b.getList(ReadBuffer.getINetworkSaveableFunction(Point.class))));
                grid.getNodes().forEach(node -> {
                    final Map<ModeSet, SubsidiaryState> subsidiares = new HashMap<>(
                            node.getSubsidiaryStates());
                    if (!subsidiares.isEmpty()) {
                        enabledSubsidiaryTypes.put(node.getPoint(), subsidiares);
                    }
                });
                update();
                loadPossibleSubsidiaires();
                break;
            }
            case SEND_PW_UPDATE: {
                colorUpdates.accept(grid.readUpdateNetwork(buffer, true));
                break;
            }
            case PW_REQUEST_RESPONSE: {
                final PathwayRequestMode result = buffer.getEnumValue(PathwayRequestMode.class);
                infoUpdates.accept(I18Wrapper.format("error." + result.getName()));
                break;
            }
            case ADDED_TO_SAVER: {
                final PathwayRequestMode result = buffer.getEnumValue(PathwayRequestMode.class);
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                final PathType type = buffer.getEnumValue(PathType.class);
                nextPathways.put(Maps.immutableEntry(start, end), type);
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
            case SET_SIGNAL_STATE: {
                grid.readUpdateNetwork(buffer, false).forEach(updateSignalState);
                break;
            }
            case SEND_COUNTER: {
                grid.setCounter(buffer.getInt());
                counterUpdater.run();
                break;
            }
            case SEND_TRAIN_NUMBER: {
                trainNumberUpdater.accept(buffer.getList(buf -> {
                    final Point point = Point.of(buffer);
                    final SignalBoxNode node = grid.getNode(point);
                    node.readNetwork(buffer);
                    return node;
                }));
                break;
            }
            case SEND_DEBUG_POINTS: {
                debugPoints.accept(
                        buffer.getList(ReadBuffer.getINetworkSaveableFunction(Point.class)));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        if (grid == null) {
            grid = tile.getSignalBoxGrid();
        }
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
            case SEND_ZS6_ENTRY: {
                deserializeEntry(buffer, buffer.getTcBoolean());
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
                    grid.count();
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
                final PathType type = buffer.getEnumValue(PathType.class);
                final PathwayRequestResult request = grid.requestWay(start, end, type);
                if (!request.wasSuccesfull()) {
                    final SignalBoxNode endNode = grid.getNode(end);
                    if (request.canBeAddedToSaver(type) && !endNode.containsOutConnection()
                            && grid.addNextPathway(start, end, type)) {
                        final WriteBuffer sucess = new WriteBuffer();
                        sucess.putEnumValue(SignalBoxNetwork.ADDED_TO_SAVER);
                        sucess.putEnumValue(request.getMode());
                        start.writeNetwork(sucess);
                        end.writeNetwork(sucess);
                        sucess.putEnumValue(type);
                        OpenSignalsMain.network.sendTo(info.player, sucess);
                        break;
                    }
                    final WriteBuffer error = new WriteBuffer();
                    error.putEnumValue(SignalBoxNetwork.PW_REQUEST_RESPONSE);
                    error.putEnumValue(request.getMode());
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
                final SubsidiaryState entry = SubsidiaryState.of(buffer);
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                final boolean enable = buffer.getBoolean();
                updateServerSubsidiary(point, modeSet, entry, enable);
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
                grid.setCounter(buffer.getInt());
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
                deserializeEntry(buffer, buffer
                        .getList(ReadBuffer.getINetworkSaveableFunction(PosIdentifier.class)));
                break;
            }
            case SEND_CONNECTED_TRAINNUMBERS: {
                deserializeEntry(buffer, ModeIdentifier.of(buffer));
                break;
            }
            case SET_SIGNAL_STATE: {
                final Point point = Point.of(buffer);
                final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
                final Rotation rotation = deserializeRotation(buffer);
                final SignalState state = buffer.getEnumValue(SignalState.class);
                grid.getNodeChecked(point)
                        .ifPresent(node -> node.updateState(new ModeSet(guiMode, rotation), state));
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

    private void loadPossibleSubsidiaires() {
        posForType.forEach((pos, linkType) -> {
            if (!linkType.equals(LinkType.SIGNAL))
                return;
            final StateInfo info = new StateInfo(this.info.world, pos);
            final Block signal = info.world.getBlockState(pos).getBlock();
            if (!(signal instanceof Signal))
                return;
            final Map<SEProperty, String> properties = ClientSignalStateHandler
                    .getClientStates(info);
            final Map<SubsidiaryState, ConfigProperty> subsidiaries = SubsidiarySignalParser.SUBSIDIARY_SIGNALS
                    .get(signal);
            if (subsidiaries == null)
                return;
            final List<SubsidiaryState> validStates = new ArrayList<>();
            subsidiaries.forEach((state, config) -> {
                for (final SEProperty property : config.state.keySet()) {
                    if (properties.containsKey(property)) {
                        validStates.add(state);
                        break;
                    }
                }
            });
            possibleSubsidiaries.put(info.pos, validStates);
        });

    }

    protected void updateClientSubsidiary(final Point point, final ModeSet mode,
            final SubsidiaryState state, final boolean enable) {
        final Map<ModeSet, SubsidiaryState> map = enabledSubsidiaryTypes.computeIfAbsent(point,
                (_u) -> new HashMap<>());
        if (enable) {
            map.put(mode, state);
        } else {
            map.remove(mode);
            if (map.isEmpty()) {
                enabledSubsidiaryTypes.remove(point);
            }
        }
    }

    private void updateServerSubsidiary(final Point point, final ModeSet mode,
            final SubsidiaryState state, final boolean enable) {
        final World world = tile.getWorld();
        grid.getNodeChecked(point).ifPresent((node) -> {
            node.getOption(mode)
                    .ifPresent(entry -> entry.getEntry(PathEntryType.SIGNAL).ifPresent(pos -> {
                        final Signal signal = SignalBoxHandler
                                .getSignal(new StateInfo(world, tile.getPos()), pos);
                        final SignalStateInfo info = new SignalStateInfo(world, pos, signal);
                        if (enable) {
                            SignalConfig.loadSubsidiary(info, state);
                            node.updateState(mode,
                                    SignalState.combine(state.getSubsidiaryShowType()));
                            node.setSubsidiaryState(mode, state);
                        } else {
                            SignalConfig.reset(new ResetInfo(info));
                            node.updateState(mode, SignalState.RED);
                            node.removeSubsidiaryState(mode);
                        }
                    }));
        });
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.tile != null) {
            this.tile.remove(this);
        }
    }

    @Override
    public EntityPlayer getPlayer() {
        return this.player;
    }

    public Map<BlockPos, LinkType> getPositionForTypes() {
        return new HashMap<>(posForType);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.player == null) {
            this.player = playerIn;
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

    protected void setConuterUpdater(final Runnable run) {
        this.counterUpdater = run;
    }

    protected void setTrainNumberUpdater(final Consumer<List<SignalBoxNode>> updater) {
        this.trainNumberUpdater = updater;
    }

    protected void setDebugPointUpdater(final Consumer<List<Point>> points) {
        this.debugPoints = points;
    }
}