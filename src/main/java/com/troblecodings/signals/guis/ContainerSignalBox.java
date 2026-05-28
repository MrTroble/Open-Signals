package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
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
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler.SignalBoxNetworkListener;
import com.troblecodings.signals.network.SignalBoxNetworkReader;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;
import com.troblecodings.signals.signalbox.MainSignalIdentifier;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.tileentitys.IChunkLoadable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerSignalBox extends ContainerBase
        implements UIClientSync, IChunkLoadable, SignalBoxNetworkReader {

    protected final Map<Point, List<MainSignalIdentifier>> greenSignals = new HashMap<>();
    protected final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = new HashMap<>();
    protected final Map<Point, Map<ModeSet, SubsidiaryState>> enabledSubsidiaryTypes =
            new HashMap<>();
    protected final Map<Map.Entry<Point, Point>, PathType> nextPathways = new HashMap<>();
    protected final Map<BlockPos, List<Point>> validInConnections = new HashMap<>();
    protected SignalBoxGrid grid;
    protected SignalBoxTileEntity tile;
    protected SignalBoxNetworkHandler network;

    private final Map<BlockPos, LinkType> posForType = new HashMap<>();
    private EntityPlayer player;
    private SignalBoxNetworkListener listener;

    protected Consumer<SignalBoxNode> updateSignalState = (node) -> {
    };
    protected Consumer<String> infoUpdates = (label) -> {
    };
    protected BiConsumer<SignalBoxNode, PathEntryType<?>> nodeUpdate = (node, entry) -> {
    };
    protected Runnable counterUpdater = () -> {
    };
    protected Consumer<List<Point>> debugPoints = (list) -> {
    };

    public ContainerSignalBox(final GuiInfo info) {
        super(info);
        this.tile = info.getTile();
        this.grid = tile.getSignalBoxGrid();
        initializeNetwork();
    }

    @Override
    public void sendAllDataToRemote() {
        this.grid = tile.getSignalBoxGrid();
        this.network = grid.getNetwork();
        initializeNetwork();
        network.sendAll(this);
    }

    @Override
    public void addAdditionalInitialisationData(final WriteBuffer buffer) {
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
    }

    @Override
    public void readAdditionalInitialisationData(final ReadBuffer buffer) {
        posForType.clear();
        nextPathways.clear();
        validInConnections.clear();
        posForType.putAll(buffer.getMap(ReadBuffer.BLOCKPOS_FUNCTION,
                ReadBuffer.getEnumFunction(LinkType.class)));
        nextPathways.putAll(buffer.getMap((b -> Maps.immutableEntry(Point.of(b), Point.of(b))),
                b -> b.getEnumValue(PathType.class)));
        validInConnections.putAll(buffer.getMap(ReadBuffer.BLOCKPOS_FUNCTION,
                b -> b.getList(ReadBuffer.getINetworkSaveableFunction(Point.class))));
        grid.getNodes().forEach(node -> {
            final Map<ModeSet, SubsidiaryState> subsidiares =
                    new HashMap<>(node.getSubsidiaryStates());
            if (!subsidiares.isEmpty()) {
                enabledSubsidiaryTypes.put(node.getPoint(), subsidiares);
            }
        });
        loadPossibleSubsidiaires();
        update();
    }

    public SignalBoxNetworkHandler getNetwork() {
        return network;
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        if (grid == null) {
            this.grid = tile.getSignalBoxGrid();
            initializeNetwork();
        }
        network.desirializeBuffer(buffer);
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        if (grid == null) {
            grid = tile.getSignalBoxGrid();
        }
        MonitorNetworkHandler.checkForClientUpdates(getStateInfo(), network,
                new WriteBuffer(buffer.getCopiedBuffer().array()));
        network.desirializeBuffer(buffer);
        tile.markDirty();
    }

    @Override
    public void handlePathwayRequestResponse(final PathwayRequestMode result) {
        if (!isClientSide())
            return;
        infoUpdates.accept(I18Wrapper.format("error." + result.getName()));
    }

    @Override
    public void handleAddSavedPathway(final Point p1, final Point p2, final PathType type,
            final PathwayRequestMode result) {
        if (!isClientSide())
            return;
        nextPathways.put(Maps.immutableEntry(p1, p2), type);
        infoUpdates.accept(I18Wrapper.format("error." + result.getName()) + " - "
                + I18Wrapper.format("info.pathwaysaver"));
    }

    @Override
    public void handleRemoveSavedPathway(final Point p1, final Point p2) {
        nextPathways.remove(Maps.immutableEntry(p1, p2));
    }

    @Override
    public void handleDebugPoints(final List<Point> debugPoints) {
        this.debugPoints.accept(debugPoints);
    }

    @Override
    public void handleCounterUpdate() {
        counterUpdater.run();
    }

    @Override
    public void onEntryUpdate(final SignalBoxNode node, final PathEntryType<?> type) {
        nodeUpdate.accept(node, type);
    }

    @Override
    public void onNodeUpdate(final SignalBoxNode node) {
        updateSignalState.accept(node);
    }

    private void initializeNetwork() {
        if (grid == null)
            return;
        listener = new SignalBoxNetworkListener(getStateInfo(),
                b -> OpenSignalsMain.network.sendTo(getPlayer(), b));
        network.addListener(listener);
        network.setUpNetworkReader(this);
    }

    private void loadPossibleSubsidiaires() {
        posForType.forEach((pos, linkType) -> {
            if (!linkType.equals(LinkType.SIGNAL))
                return;
            final StateInfo info = new StateInfo(this.info.world, pos);
            final Block signal = info.world.getBlockState(pos).getBlock();
            if (!(signal instanceof Signal))
                return;
            final Map<SEProperty, String> properties =
                    ClientSignalStateHandler.getClientStates(info);
            final Map<SubsidiaryState, ConfigProperty> subsidiaries =
                    SubsidiarySignalParser.SUBSIDIARY_SIGNALS.get(signal);
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

    protected void updateClientSubsidiary(final SignalBoxNode node, final ModeSet mode,
            final SubsidiaryState state, final boolean enable) {
        final Map<ModeSet, SubsidiaryState> map =
                enabledSubsidiaryTypes.computeIfAbsent(node.getPoint(), (_u) -> new HashMap<>());
        if (enable) {
            map.put(mode, state);
            node.setSubsidiaryState(mode, state);
        } else {
            map.remove(mode);
            node.removeSubsidiaryState(mode);
            if (map.isEmpty()) {
                enabledSubsidiaryTypes.remove(node.getPoint());
            }
        }
    }

    @Override
    public void updateServerSubsidiary(final ModeIdentifier ident, final SubsidiaryState state,
            final boolean enable) {
        if (isClientSide())
            return;
        final World world = tile.getWorld();
        grid.getNodeChecked(ident.point).ifPresent((node) -> {
            node.getOption(ident.mode)
                    .ifPresent(entry -> entry.getEntry(PathEntryType.SIGNAL).ifPresent(pos -> {
                        final Signal signal = SignalBoxHandler
                                .getSignal(new StateInfo(world, tile.getPos()), pos);
                        final SignalStateInfo info = new SignalStateInfo(world, pos, signal);
                        if (enable) {
                            SignalConfig.loadSubsidiary(info, state);
                            node.setSubsidiaryState(ident.mode, state);
                            node.updateStateNoNetwork(ident.mode,
                                    SignalState.combine(state.getSubsidiaryShowType()));
                        } else {
                            SignalConfig.reset(new ResetInfo(info));
                            node.removeSubsidiaryState(ident.mode);
                            node.updateStateNoNetwork(ident.mode, SignalState.RED);
                        }
                    }));
        });
    }

    public SignalBoxTileEntity getTile() {
        return this.tile;
    }

    @Override
    public SignalBoxGrid getGrid() {
        return this.grid;
    }

    @Override
    public boolean isClientSide() {
        return this.info.world.isRemote;
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        network.removeListener(listener);
        network.removeNetworkReader();
        if (this.tile != null) {
            this.tile.remove(this);
        }
    }

    @Override
    public EntityPlayer getPlayer() {
        return this.info.player;
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

    @Override
    public StateInfo getStateInfo() {
        return new StateInfo(info.world, tile.getPos());
    }
}