package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;

public class SubsidiaryHolder {

    public final SubsidiaryEntry entry;
    public final Point point;
    public final ModeSet modeSet;

    public SubsidiaryHolder(final SubsidiaryEntry entry, final Point point, final ModeSet modeSet) {
        this.entry = entry;
        this.point = point;
        this.modeSet = modeSet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entry, modeSet, point);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubsidiaryHolder other = (SubsidiaryHolder) obj;
        return Objects.equals(entry, other.entry) && Objects.equals(modeSet, other.modeSet)
                && Objects.equals(point, other.point);
    }
}