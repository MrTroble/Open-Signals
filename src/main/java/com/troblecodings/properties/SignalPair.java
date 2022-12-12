package com.troblecodings.properties;

import java.util.Objects;

import com.troblecodings.signals.blocks.Signal;

public class SignalPair {

    public final Signal start;
    public final Signal end;

    public SignalPair(final Signal start, final Signal end) {
        super();
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalPair other = (SignalPair) obj;
        return Objects.equals(end, other.end) && Objects.equals(start, other.start);
    }
}