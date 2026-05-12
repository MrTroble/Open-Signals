package com.troblecodings.signals.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;

public class SignalBoxNetworkMode {

    private static final List<SignalBoxNetworkMode> NETWORK_ENTRIES = new ArrayList<>();

    private int id;
    private final BiConsumer<ReadBuffer, SignalBoxNetworkHandler> read;

    public SignalBoxNetworkMode(final BiConsumer<ReadBuffer, SignalBoxNetworkHandler> read) {
        this.read = read;
        this.id = NETWORK_ENTRIES.size();
        NETWORK_ENTRIES.add(this);
    }

    public static SignalBoxNetworkMode getModeFromBuffer(final ReadBuffer buffer) {
        return NETWORK_ENTRIES.get(buffer.getInt());
    }

    public void executeRead(final ReadBuffer buffer, final SignalBoxNetworkHandler network) {
        read.accept(buffer, network);
    }

    public WriteBuffer getBuffer() {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putInt(id);
        return buffer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, read);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalBoxNetworkMode other = (SignalBoxNetworkMode) obj;
        return id == other.id && Objects.equals(read, other.read);
    }

}