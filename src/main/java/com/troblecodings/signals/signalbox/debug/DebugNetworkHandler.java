package com.troblecodings.signals.signalbox.debug;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;

public class DebugNetworkHandler extends SignalBoxNetworkHandler {

    private final SignalBoxGrid grid;

    public DebugNetworkHandler(final SignalBoxGrid grid) {
        this.grid = grid;
    }

    @Override
    protected boolean containerConnected() {
        return true;
    }

    @Override
    protected SignalBoxGrid getGrid() {
        return grid;
    }

    @Override
    protected void sendBuffer(final WriteBuffer buffer) {
        desirializeBuffer(new ReadBuffer(
                Unpooled.copiedBuffer(buffer.getBuildedBuffer().position(0)).nioBuffer()));
    }

    @Override
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
        }
        if (mode.equals(NodeNetworkMode.MANUELL_OUTPUT_ADD)
                || mode.equals(NodeNetworkMode.MANUELL_OUTPUT_REMOVE)) {
            node.handleManuellEnabledOutputUpdate(buffer.getINetworkSaveable(ModeSet.class),
                    mode.equals(NodeNetworkMode.MANUELL_OUTPUT_ADD) ? true : false);
        }
        if (mode.equals(NodeNetworkMode.SIGNAL_STATE)) {
            node.readSignalStates(buffer);
        }
    }

    @Override
    protected void readForGrid(final ReadBuffer buffer) {
        final SignalBoxGrid grid = getGrid();
        final GridNetworkMode mode = buffer.getEnumValue(GridNetworkMode.class);
        if (mode.equals(GridNetworkMode.SEND_ALL)) {
            grid.readNetwork(buffer);
        } else if (mode.equals(GridNetworkMode.COUNTER)) {
            grid.setCounterFromNetwork(buffer.getInt());
        } else {
            final BlockPos pos = buffer.getBlockPos();
            SignalBoxHandler.unlinkPosFromSignalBox(new StateInfo(container.getTile().getLevel(),
                    container.getTile().getBlockPos()), pos);
        }
    }

    @Override
    protected void readEntry(final ReadBuffer buffer) {
        final EntryNetworkMode mode = buffer.getEnumValue(EntryNetworkMode.class);
        final ModeIdentifier ident = ModeIdentifier.of(buffer);
        final SignalBoxNode node = getGrid().getOrCreateNode(ident.point);
        if (mode.equals(EntryNetworkMode.MODE_ADD) || mode.equals(EntryNetworkMode.MODE_REMOVE)) {
            node.applyModeNetworkChanges(ident.mode);
            return;
        }
        final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES.get(buffer.getInt());
        final PathOptionEntry optionEntry = node.getOrCreateOption(ident.mode);
        if (mode.equals(EntryNetworkMode.ENTRY_REMOVE)) {
            optionEntry.removeEntryNoNetwork(entryType);
            return;
        }
        final IPathEntry<?> entry = entryType.newValue();
        entry.readNetwork(buffer);
        optionEntry.addEntry(entryType, entry);
    }

}