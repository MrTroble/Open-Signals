package com.troblecodings.signals.network;

import java.util.List;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public interface SignalBoxNetworkReader {

    default void addAdditionalInitialisationData(final WriteBuffer buffer) {
    }

    default void readAdditionalInitialisationData(final ReadBuffer buffer) {
    }

    default void handleNodeUpdate(final SignalBoxNode node, final PathEntryType<?> entryType) {
    }

    default void handleCounterUpdate() {
    }

    default void handleUIProfileUpdate() {
    }

    default void handleSignalStateUpdate(final SignalBoxNode node) {
    }

    default void handlePathwayRequestResponse(final PathwayRequestMode mode) {
    }

    default void handleRemoveSavedPathway(final Point p1, final Point p2) {
    }

    default void handleAddSavedPathway(final Point p1, final Point p2, final PathType type,
            final PathwayRequestMode result) {
    }

    default void updateServerSubsidiary(final ModeIdentifier ident, final SubsidiaryState entry,
            final boolean state) {
    }

    default void handleDebugPoints(final List<Point> points) {
    }

    StateInfo getStateInfo();

    boolean isClientSide();

    SignalBoxGrid getGrid();

}
