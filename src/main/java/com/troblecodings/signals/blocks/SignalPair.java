package com.troblecodings.signals.blocks;

import java.util.Objects;

public class SignalPair {

    public final Signal start;
    public final Signal end;

    public SignalPair(final Signal start, final Signal end) {
        super();
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }
}