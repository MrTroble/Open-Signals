package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.LoadHolder;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.SignalStateLoadHoler;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.ChangedState;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.parser.interm.LogicalSymbols;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class SignalReaderTileEntity extends SyncableTileEntity
        implements ILinkableTile, SignalStateListener {

    public SignalReaderTileEntity(final TileEntityInfo info) {
        super(info);
    }

    public static final int MAX_PROPERTIES_SIZE = 10;
    public static final int MAX_LOGICAL_VALUES_SIZE = MAX_PROPERTIES_SIZE - 1;

    public static final String LINKED_SIGNAL_POS = "linkedSignalPos";
    public static final String PROPERTIES = "signalProperties";
    public static final String DIRECTION = "direction";
    public static final String ALL_STATES = "allStates";
    public static final String REDSTONE_ACTIVATED = "redstoneActivated";
    public static final String SIGNAL_NAME = "signalName";
    public static final String POSITION_ID = "positionID";
    public static final String LOGICAL_SYMBOLS = "logicalSymbols";

    private final Map<Direction, Map.Entry<LogicalSymbols[], Map.Entry<SEProperty, String>[]>> statesForFace =
            new HashMap<>();
    private final Map<Direction, Predicate<Map<SEProperty, String>>> predicateForDirection =
            new HashMap<>();
    private final boolean[] activatedDirections = new boolean[Direction.values().length];
    private BlockPos signalPos;
    private Signal signal;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        statesForFace.clear();
        signalPos = wrapper.getBlockPos(LINKED_SIGNAL_POS);
        signal = Signal.SIGNALS.get(wrapper.getString(SIGNAL_NAME));
        if (signalPos == null || signal == null)
            return;
        final Map<String, SEProperty> namesForProperty = signal.getProperties().stream()
                .collect(Collectors.toMap(prop -> prop.getName(), prop -> prop));
        final List<NBTWrapper> list = wrapper.getList(ALL_STATES);
        list.forEach(directionWrapper -> {
            final Direction dir = Direction.byName(directionWrapper.getString(DIRECTION));

            final LogicalSymbols[] symbols = new LogicalSymbols[MAX_LOGICAL_VALUES_SIZE];
            final List<NBTWrapper> symbolsList = directionWrapper.getList(LOGICAL_SYMBOLS);
            symbolsList.forEach(symbolWrapper -> {
                final int positionID = symbolWrapper.getInteger(POSITION_ID);
                symbols[positionID] = LogicalSymbols.find(symbolWrapper.getString(LOGICAL_SYMBOLS));
            });

            @SuppressWarnings("unchecked")
            final Entry<SEProperty, String>[] propertyEntries = new Entry[MAX_PROPERTIES_SIZE];
            final List<NBTWrapper> propsList = directionWrapper.getList(PROPERTIES);
            propsList.forEach(propertyWrapper -> {
                final int positionID = propertyWrapper.getInteger(POSITION_ID);
                propertyWrapper.keySet().stream().filter(str -> !str.equals(POSITION_ID))
                        .forEach(key -> {
                            final SEProperty prop = namesForProperty.get(key);
                            propertyEntries[positionID] = Maps.immutableEntry(prop,
                                    prop.readFromNBT(propertyWrapper).get());
                        });
            });

            statesForFace.put(dir, Maps.immutableEntry(symbols, propertyEntries));
            activatedDirections[dir.ordinal()] = directionWrapper.getBoolean(REDSTONE_ACTIVATED);
        });
        generatePredicatesFor(Direction.values());
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        if (signalPos == null || signal == null)
            return;
        wrapper.putBlockPos(LINKED_SIGNAL_POS, signalPos);
        wrapper.putString(SIGNAL_NAME, signal.getSignalTypeName());
        final List<NBTWrapper> list = new ArrayList<>();
        statesForFace.forEach((dir, entry) -> {
            final NBTWrapper directionWrapper = new NBTWrapper();
            directionWrapper.putString(DIRECTION, dir.getName());

            final List<NBTWrapper> propsList = new ArrayList<>();
            final Entry<SEProperty, String>[] properties = entry.getValue();
            for (int i = 0; i < properties.length; i++) {
                final Entry<SEProperty, String> property = properties[i];
                if (property != null) {
                    final NBTWrapper propertyWrapper = new NBTWrapper();
                    propertyWrapper.putInteger(POSITION_ID, i);
                    property.getKey().writeToNBT(propertyWrapper, property.getValue());
                    propsList.add(propertyWrapper);
                }
            }
            directionWrapper.putList(PROPERTIES, propsList);

            final List<NBTWrapper> symbolsList = new ArrayList<>();
            final LogicalSymbols[] symbols = entry.getKey();
            for (int i = 0; i < symbols.length; i++) {
                final LogicalSymbols symbol = symbols[i];
                if (symbol != null) {
                    final NBTWrapper symbolWrapper = new NBTWrapper();
                    symbolWrapper.putInteger(POSITION_ID, i);
                    symbolWrapper.putString(LOGICAL_SYMBOLS, symbol.symbol);
                    symbolsList.add(symbolWrapper);
                }
            }
            directionWrapper.putList(LOGICAL_SYMBOLS, symbolsList);

            if (activatedDirections[dir.ordinal()]) {
                directionWrapper.putBoolean(REDSTONE_ACTIVATED, true);
            }
            list.add(directionWrapper);
        });
        wrapper.putList(ALL_STATES, list);
    }

    public Signal getLinkedSignalBlock() {
        return signal;
    }

    public BlockPos getLinkedSignalPos() {
        return signalPos;
    }

    public boolean enableRedstoneForDirection(final Direction dir) {
        return activatedDirections[dir.ordinal()];
    }

    public Map<Direction, Entry<LogicalSymbols[], Entry<SEProperty, String>[]>> getPropertiesForFace() {
        return ImmutableMap.copyOf(statesForFace);
    }

    public void removeDirection(final Direction dir) {
        statesForFace.remove(dir);
        predicateForDirection.remove(dir);
    }

    public void setUpForDirection(final Direction dir,
            final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entry) {
        statesForFace.put(dir, entry);
        generatePredicatesFor(dir);
    }

    private void generatePredicatesFor(final Direction... directions) {
        for (final Direction dir : directions) {
            final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entry =
                    statesForFace.get(dir);
            if (entry == null) {
                continue;
            }
            Predicate<Map<SEProperty, String>> predicate = map -> false;
            for (int i = 0; i < entry.getValue().length; i++) {
                final Entry<SEProperty, String> propertyEntry = entry.getValue()[i];
                if (propertyEntry == null) {
                    continue;
                }

                final Predicate<Map<SEProperty, String>> currentPredicate =
                        getPredicateFromEntry(propertyEntry);
                final int logicalSymbolID = i - 1;
                if (logicalSymbolID >= 0) {
                    final LogicalSymbols symbol = entry.getKey()[logicalSymbolID];
                    if (symbol.equals(LogicalSymbols.AND)) {
                        predicate = predicate.and(currentPredicate);
                    } else {
                        predicate = predicate.or(currentPredicate);
                    }
                } else {
                    predicate = currentPredicate;
                }
            }
            predicateForDirection.put(dir, predicate);
        }
    }

    private static Predicate<Map<SEProperty, String>> getPredicateFromEntry(
            final Entry<SEProperty, String> entry) {
        return map -> map.getOrDefault(entry.getKey(), "").equals(entry.getValue());
    }

    @Override
    public void onLoad() {
        if (level.isClientSide || signalPos == null || signal == null)
            return;
        final SignalStateInfo info = new SignalStateInfo(level, signalPos, signal);
        SignalStateHandler.addListener(info, this);
        SignalStateHandler
                .loadSignal(new SignalStateLoadHoler(info, new LoadHolder<>(getBlockPos())));
    }

    @Override
    public void onChunkUnloaded() {
        if (level.isClientSide || signalPos == null || signal == null)
            return;
        final SignalStateInfo info = new SignalStateInfo(level, signalPos, signal);
        SignalStateHandler.removeListener(info, this);
        SignalStateHandler
                .unloadSignal(new SignalStateLoadHoler(info, new LoadHolder<>(getBlockPos())));
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toShortString())));
        if (block instanceof Signal) {
            signal = (Signal) block;
            signalPos = pos;
            onLoad();
            return true;
        }
        return false;
    }

    @Override
    public boolean hasLink() {
        return signal != null && signalPos != null;
    }

    @Override
    public boolean unlink() {
        signal = null;
        signalPos = null;
        statesForFace.clear();
        onChunkUnloaded();
        return true;
    }

    @Override
    public void update(final SignalStateInfo info, final Map<SEProperty, String> changedProperties,
            final ChangedState changedState) {
        if (info.isWorldNullOrClientSide())
            return;
        if (changedState.equals(ChangedState.REMOVED_FROM_FILE)) {
            unlink();
            return;
        }
        if (changedState.equals(ChangedState.ADDED_TO_CACHE)
                || changedState.equals(ChangedState.UPDATED)) {
            final Map<SEProperty, String> allProperties = SignalStateHandler.getStates(info);
            for (final Direction dir : Direction.values()) {
                activatedDirections[dir.ordinal()] = false;
                if (predicateForDirection.getOrDefault(dir, _u -> false).test(allProperties)) {
                    activatedDirections[dir.ordinal()] = true;
                }
            }
            level.blockUpdated(worldPosition, getBlockState().getBlock());
        }
    }
}