package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.signals.core.ModeIdentifier;

import net.minecraft.core.BlockPos;

public class MainSignalIdentifier {

    public final ModeIdentifier identifier;
    public final BlockPos pos;
    public SignalState state = SignalState.RED;

    public MainSignalIdentifier(final Point point, final ModeSet mode, final BlockPos pos) {
        this.identifier = new ModeIdentifier(point, mode);
        this.pos = pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, pos);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MainSignalIdentifier other = (MainSignalIdentifier) obj;
        return Objects.equals(identifier, other.identifier) && Objects.equals(pos, other.pos);
    }

    public static enum SignalState {

        RED, GREEN;

    }
}