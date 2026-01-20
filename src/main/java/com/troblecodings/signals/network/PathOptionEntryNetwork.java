package com.troblecodings.signals.network;

import java.util.Objects;

import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public class PathOptionEntryNetwork {

    private SignalBoxNetworkHandler network;
    private ModeIdentifier ident;

    public void sendEntryAdd(final PathEntryType<?> type, final IPathEntry<?> entry) {
        if (network == null)
            throw new IllegalArgumentException(
                    "Tried to send entry without network beeing connected!");
        network.sendEntryAdd(ident, type, entry);
    }

    public void sendEntryRemove(final PathEntryType<?> type) {
        if (network == null)
            throw new IllegalArgumentException(
                    "Tried to send entry without network beeing connected!");
        network.sendEntryRemove(ident, type);
    }

    public PathOptionEntryNetwork setUpNetwork(final SignalBoxNetworkHandler network,
            final ModeIdentifier ident) {
        this.network = network;
        this.ident = ident;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, network);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final PathOptionEntryNetwork other = (PathOptionEntryNetwork) obj;
        return Objects.equals(ident, other.ident) && Objects.equals(network, other.network);
    }

}