package com.troblecodings.signals.network;

import java.util.Objects;

import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public class PathOptionEntryNetworkHandler {

    private SignalBoxNetworkHandler network = null;
    private ModeIdentifier ident = null;

    public void setUpNetwork(final SignalBoxNetworkHandler network, final ModeIdentifier ident) {
        this.network = network;
        this.ident = ident;
    }

    public void removeNetwork() {
        this.network = null;
        this.ident = null;
    }

    private boolean isNetworkConnected() {
        return network != null && ident != null;
    }

    public <T> void sendEntryAdd(final PathEntryType<T> entryType, final IPathEntry<T> entry) {
        if (!isNetworkConnected())
            return;
        network.sendEntryAdd(ident, entryType, entry);
    }

    public <T> void sendEntryRemove(final PathEntryType<T> entryType) {
        if (!isNetworkConnected())
            return;
        network.sendEntryRemove(ident, entryType);
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
        final PathOptionEntryNetworkHandler other = (PathOptionEntryNetworkHandler) obj;
        return Objects.equals(ident, other.ident) && Objects.equals(network, other.network);
    }

}
