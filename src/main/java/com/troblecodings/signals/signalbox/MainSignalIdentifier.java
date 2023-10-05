package com.troblecodings.signals.signalbox;

import java.util.Objects;

import net.minecraft.core.BlockPos;

public class MainSignalIdentifier {

    public final Point point;
    public final ModeSet mode;
    public final BlockPos pos;

    public MainSignalIdentifier(final Point point, final ModeSet mode, final BlockPos pos) {
        this.point = point;
        this.mode = mode;
        this.pos = pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, point, pos);
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
        return Objects.equals(mode, other.mode) && Objects.equals(point, other.point)
                && Objects.equals(pos, other.pos);
    }
}