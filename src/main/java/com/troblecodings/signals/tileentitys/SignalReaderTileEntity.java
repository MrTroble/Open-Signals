package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.opensignals.linkableapi.ILinkableTile;
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

    public static final String LINKED_SIGNAL_POS = "linkedSignalPos";
    public static final String PROPERTIES = "signalProperties";
    public static final String DIRECTION = "direction";
    public static final String ALL_STATES = "allStates";
    public static final String REDSTONE_ACTIVATED = "redstoneActivated";
    public static final String SIGNAL_NAME = "signalName";

    private final Map<Direction, Map<SEProperty, String>> statesForFace = new HashMap<>();
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
        final List<NBTWrapper> list = wrapper.getList(ALL_STATES);
        list.forEach(directionWrapper -> {
            final Direction dir = Direction.byName(directionWrapper.getString(DIRECTION));
            final NBTWrapper propsWrapper = directionWrapper.getWrapper(PROPERTIES);
            final Map<SEProperty, String> properties = new HashMap<>();
            signal.getProperties().forEach(prop -> prop.readFromNBT(propsWrapper)
                    .ifPresent(value -> properties.put(prop, value)));
            statesForFace.put(dir, properties);
            activatedDirections[dir.ordinal()] = directionWrapper.getBoolean(REDSTONE_ACTIVATED);
        });
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        if (signalPos == null || signal == null)
            return;
        wrapper.putBlockPos(LINKED_SIGNAL_POS, signalPos);
        wrapper.putString(SIGNAL_NAME, signal.getSignalTypeName());
        final List<NBTWrapper> list = new ArrayList<>();
        statesForFace.forEach((dir, map) -> {
            if (map.isEmpty())
                return;
            final NBTWrapper directionWrapper = new NBTWrapper();
            directionWrapper.putString(DIRECTION, dir.getName());

            final NBTWrapper propsWrapper = new NBTWrapper();
            map.forEach((prop, value) -> prop.writeToNBT(propsWrapper, value));
            directionWrapper.putWrapper(PROPERTIES, propsWrapper);
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

    public Map<Direction, Map<SEProperty, String>> getPropertiesForFace() {
        return ImmutableMap.copyOf(statesForFace);
    }

    public void addPropertyToDirection(final Direction dir, final SEProperty property,
            final String value) {
        statesForFace.computeIfAbsent(dir, _u -> new HashMap<>()).put(property, value);
    }

    public void removePropertyFromDirection(final Direction dir, final SEProperty property) {
        statesForFace.computeIfAbsent(dir, _u -> new HashMap<>()).remove(property);
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
        if (changedState.equals(ChangedState.REMOVED_FROM_FILE)) {
            unlink();
            return;
        }
        if (changedState.equals(ChangedState.ADDED_TO_CACHE)
                || changedState.equals(ChangedState.UPDATED)) {
            final Map<SEProperty, String> allProperties = SignalStateHandler.getStates(info);
            statesForFace.forEach((dir, properties) -> {
                if (properties.isEmpty()) {
                    activatedDirections[dir.ordinal()] = false;
                    return;
                }
                for (final Entry<SEProperty, String> entry : properties.entrySet()) {
                    if (!allProperties.get(entry.getKey()).equals(entry.getValue())) {
                        activatedDirections[dir.ordinal()] = false;
                        return;
                    }
                }
                activatedDirections[dir.ordinal()] = true;
            });
            level.blockUpdated(worldPosition, getBlockState().getBlock());
        }
    }
}