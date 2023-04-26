package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.signalbox.config.SignalConfig;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkedPositions {

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String LINKED_SIGNALS = "linkedSignals";
    private static final String SIGNAL_NAME = "signalName";
    private static final String ALL_POS = "allPos";

    private final Map<BlockPos, Signal> signals;
    private final Map<BlockPos, LinkType> linkedBlocks;

    public LinkedPositions() {
        signals = new HashMap<>();
        linkedBlocks = new HashMap<>();
    }

    public void addSignal(final BlockPos signalPos, final Signal signal, final World world) {
        signals.put(signalPos, signal);
        SignalConfig.reset(new SignalStateInfo(world, signalPos, signal));
    }

    public Signal getSignal(final BlockPos pos) {
        return signals.get(pos);
    }

    public boolean addLinkedPos(final BlockPos pos, final LinkType type) {
        if (linkedBlocks.containsKey(pos))
            return false;
        linkedBlocks.put(pos, type);
        return true;
    }

    public void removeLinkedPos(final BlockPos pos) {
        final LinkType type = linkedBlocks.remove(pos);
        if (type != null && type.equals(LinkType.SIGNAL))
            signals.remove(pos);
    }

    public boolean isEmpty() {
        return linkedBlocks.isEmpty();
    }

    public Map<BlockPos, LinkType> getAllLinkedPos() {
        return ImmutableMap.copyOf(linkedBlocks);
    }

    public void unlink(final BlockPos tilePos, final World world) {
        signals.forEach(
                (pos, signal) -> SignalConfig.reset(new SignalStateInfo(world, pos, signal)));
        linkedBlocks.entrySet().stream().filter(entry -> !entry.getValue().equals(LinkType.SIGNAL))
                .forEach(entry -> SignalBoxHandler
                        .unlinkTileFromPos(new PosIdentifier(tilePos, world), entry.getKey()));
        linkedBlocks.clear();
        signals.clear();
    }

    public void write(final NBTWrapper wrapper) {
        final NBTWrapper posWrapper = new NBTWrapper();
        posWrapper.putList(LINKED_POS_LIST, linkedBlocks.entrySet().stream().map(entry -> {
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
        });
    }

    public void loadSignals(final World world) {
        if (world.isClientSide)
            return;
        final List<SignalStateInfo> signalInfos = new ArrayList<>();
        signals.forEach((pos, signal) -> signalInfos.add(new SignalStateInfo(world, pos, signal)));
        SignalStateHandler.loadSignals(signalInfos);
    }

    public void unloadSignals(final World world) {
        if (world.isClientSide)
            return;
        final List<SignalStateInfo> signalInfos = new ArrayList<>();
        signals.forEach((pos, signal) -> signalInfos.add(new SignalStateInfo(world, pos, signal)));
        SignalStateHandler.unloadSignals(signalInfos);
    }

    @Override
    public String toString() {
        return "AllSignals = " + signals + ", AllLinkedPos = " + linkedBlocks;
    }
}