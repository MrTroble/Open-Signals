package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.enums.SubsidiaryType;

public class SubsidiaryEntry {

    public final SubsidiaryType type;
    public final boolean state;

    public SubsidiaryEntry(final SubsidiaryType type, final boolean state) {
        this.type = type;
        this.state = state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, type);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubsidiaryEntry other = (SubsidiaryEntry) obj;
        return state == other.state && type == other.type;
    }

}
