package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.SubsidiarySignalParser;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;
import com.troblecodings.signals.signalbox.config.ResetInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkedPositions {

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String LINKED_SIGNALS = "linkedSignals";
    private static final String SIGNAL_NAME = "signalName";
    private static final String ALL_POS = "allPos";

    private final BlockPos thisPos;
    private final Map<BlockPos, Signal> signals = new HashMap<>();
    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final Map<BlockPos, List<SubsidiaryState>> possibleSubsidiaries = new HashMap<>();

    public LinkedPositions(final BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    public void addSignal(final BlockPos signalPos, final Signal signal, final World world) {
        if (world.isRemote)
            return;
        signals.put(signalPos, signal);
        final SignalStateInfo info = new SignalStateInfo(world, signalPos, signal);
        final LoadHolder<StateInfo> holder = new LoadHolder<>(new StateInfo(world, thisPos));
        SignalStateHandler.loadSignal(new SignalStateLoadHoler(info, holder));
        SignalStateHandler.runTaskWhenSignalLoaded(info,
                (stateInfo, properties, _u) -> loadPossibleSubsidiaires(stateInfo, properties));
        SignalConfig.reset(new ResetInfo(info, false));
    }

    public Signal getSignal(final BlockPos pos) {
        return signals.get(pos);
    }

    public boolean addLinkedPos(final BlockPos pos, final World world, final LinkType type) {
        if (linkedBlocks.containsKey(pos))
            return false;
        linkedBlocks.put(pos, type);
        final StateInfo info = new StateInfo(world, pos);
        NameHandler.loadName(
                new StateLoadHolder(info, new LoadHolder<>(new StateInfo(world, thisPos))));
        return true;
    }

    public void removeLinkedPos(final BlockPos pos, final World world) {
        if (world.isRemote)
            return;
        final StateInfo info = new StateInfo(world, thisPos);
        final LinkType type = linkedBlocks.remove(pos);
        if (type != null && type == LinkType.SIGNAL) {
            final Signal signal = signals.remove(pos);
            possibleSubsidiaries.remove(pos);
            SignalStateHandler.unloadSignal(new SignalStateLoadHoler(
                    new SignalStateInfo(world, pos, signal), new LoadHolder<>(info)));
        }
        if (type != null && type != LinkType.SIGNALBOX)
            NameHandler.unloadName(
                    new StateLoadHolder(new StateInfo(world, pos), new LoadHolder<>(info)));
    }

    public boolean isEmpty() {
        return linkedBlocks.isEmpty();
    }

    public Map<BlockPos, LinkType> getAllLinkedPos() {
        return ImmutableMap.copyOf(linkedBlocks);
    }

    public void unlink(final BlockPos tilePos, final World world) {
        if (world.isRemote)
            return;
        final List<SignalStateLoadHoler> signalsToUnload = new ArrayList<>();
        signals.forEach((pos, signal) -> {
            final SignalStateInfo info = new SignalStateInfo(world, pos, signal);
            SignalConfig.reset(new ResetInfo(info, false));
            signalsToUnload.add(new SignalStateLoadHoler(info,
                    new LoadHolder<>(new StateInfo(world, tilePos))));
        });
        linkedBlocks.entrySet().stream().filter(entry -> !entry.getValue().equals(LinkType.SIGNAL))
                .forEach(entry -> SignalBoxHandler.unlinkTileFromPos(new StateInfo(world, tilePos),
                        entry.getKey()));
        linkedBlocks.clear();
        signals.clear();
        possibleSubsidiaries.clear();
        SignalStateHandler.unloadSignals(signalsToUnload);
        NameHandler.unloadNames(linkedBlocks.entrySet().stream()
                .map(entry -> new StateLoadHolder(new StateInfo(world, entry.getKey()),
                        new LoadHolder<>(new StateInfo(world, tilePos))))
                .collect(Collectors.toList()));
    }

    public void write(final NBTWrapper wrapper) {
        final NBTWrapper posWrapper = new NBTWrapper();
        posWrapper.putList(LINKED_POS_LIST, linkedBlocks.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(LinkType.SIGNAL)).map(entry -> {
                    final NBTWrapper item = NBTWrapper.getBlockPosWrapper(entry.getKey());
                    entry.getValue().write(item);
                    return item;
                })::iterator);
        posWrapper.putList(LINKED_SIGNALS, signals.entrySet().stream().map(entry -> {
            final NBTWrapper signal = NBTWrapper.getBlockPosWrapper(entry.getKey());
            signal.putString(SIGNAL_NAME, entry.getValue().getSignalTypeName());
            return signal;
        })::iterator);
        wrapper.putWrapper(ALL_POS, posWrapper);
    }

    public void read(final NBTWrapper wrapper) {
        if (!wrapper.contains(ALL_POS))
            return;
        final NBTWrapper posWrapper = wrapper.getWrapper(ALL_POS);
        linkedBlocks.clear();
        signals.clear();
        posWrapper.getList(LINKED_POS_LIST)
                .forEach(nbt -> linkedBlocks.put(nbt.getAsPos(), LinkType.of(nbt)));
        posWrapper.getList(LINKED_SIGNALS).forEach(nbt -> {
            final BlockPos pos = nbt.getAsPos();
            final Signal signal = Signal.SIGNALS.get(nbt.getString(SIGNAL_NAME));
            signals.put(pos, signal);
            linkedBlocks.put(pos, LinkType.SIGNAL);
        });
    }

    public void loadSignals(final World world) {
        if (world.isRemote)
            return;
        final List<SignalStateLoadHoler> signalInfos = new ArrayList<>();
        final LoadHolder<StateInfo> holder = new LoadHolder<>(new StateInfo(world, thisPos));
        signals.forEach((pos, signal) -> {
            final SignalStateInfo info = new SignalStateInfo(world, pos, signal);
            signalInfos.add(new SignalStateLoadHoler(info, holder));
            SignalStateHandler.runTaskWhenSignalLoaded(info,
                    (stateInfo, properties, _u) -> loadPossibleSubsidiaires(stateInfo, properties));
        });
        SignalStateHandler.loadSignals(signalInfos);
        NameHandler.loadNames(linkedBlocks.entrySet().stream()
                .map(entry -> new StateLoadHolder(new StateInfo(world, entry.getKey()), holder))
                .collect(Collectors.toList()));
    }

    public void unloadSignals(final World world) {
        if (world.isRemote)
            return;
        final List<SignalStateLoadHoler> signalInfos = new ArrayList<>();
        final LoadHolder<StateInfo> holder = new LoadHolder<>(new StateInfo(world, thisPos));
        signals.forEach((pos, signal) -> signalInfos
                .add(new SignalStateLoadHoler(new SignalStateInfo(world, pos, signal), holder)));
        SignalStateHandler.unloadSignals(signalInfos);
        NameHandler.unloadNames(linkedBlocks.entrySet().stream()
                .map(entry -> new StateLoadHolder(new StateInfo(world, entry.getKey()), holder))
                .collect(Collectors.toList()));
        possibleSubsidiaries.clear();
    }

    private void loadPossibleSubsidiaires(final SignalStateInfo info,
            final Map<SEProperty, String> properties) {
        final Map<SubsidiaryState, ConfigProperty> subsidiaries = SubsidiarySignalParser.SUBSIDIARY_SIGNALS
                .get(info.signal);
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
    }

    public List<BlockPos> getAllRedstoneIOs() {
        return linkedBlocks.entrySet().stream()
                .filter(entry -> !entry.getValue().equals(LinkType.SIGNAL))
                .map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    public Map<BlockPos, List<SubsidiaryState>> getValidSubsidiariesForPos() {
        return ImmutableMap.copyOf(possibleSubsidiaries);
    }

    @Override
    public String toString() {
        return "LinkedPos [AllSignals = " + signals + ", AllLinkedPos = " + linkedBlocks + "]";
    }
}