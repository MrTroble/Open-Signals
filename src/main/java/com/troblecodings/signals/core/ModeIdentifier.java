package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;

public class ModeIdentifier {
    
    public final Point point;
    public final ModeSet mode;

    public ModeIdentifier(final Point point, final ModeSet mode) {
        this.point = point;
        this.mode = mode;
    }

    public void writeNetwork(final WriteBuffer buffer) {
        point.writeNetwork(buffer);
        mode.writeNetwork(buffer);
    }

    public static ModeIdentifier of(final ReadBuffer buffer) {
        return new ModeIdentifier(Point.of(buffer), ModeSet.of(buffer));
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, point);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ModeIdentifier other = (ModeIdentifier) obj;
        return Objects.equals(mode, other.mode) && Objects.equals(point, other.point);
    }

    @Override
    public String toString() {
        return "ModeIdentifier [point=" + point + ",mode=" + mode + "]";
    }
}
