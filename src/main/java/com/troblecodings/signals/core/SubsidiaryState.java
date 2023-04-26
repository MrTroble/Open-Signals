package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubsidiaryState {

    public static final List<SubsidiaryState> ALL_STATES = new ArrayList<>();

    private final int id;
    private final String name;

    public SubsidiaryState(final String name) {
        this.name = name.toLowerCase();
        this.id = ALL_STATES.size();
        ALL_STATES.add(this);
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) id);
    }

    public static SubsidiaryState of(final ReadBuffer buffer) {
        return ALL_STATES.get(buffer.getByteAsInt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SubsidiaryState other = (SubsidiaryState) obj;
        return id == other.id && Objects.equals(name, other.name);
    }
}