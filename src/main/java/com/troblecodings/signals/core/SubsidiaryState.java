package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.enums.ShowSubsidiary;

public class SubsidiaryState {

    public static final List<SubsidiaryState> ALL_STATES = new ArrayList<>();

    private transient int id;
    private transient ShowSubsidiary showSubsidiary = ShowSubsidiary.SIGNAL_RED;
    private String name;
    private String showSubsidiaryAtSignal;
    private boolean isCountable = false;

    public SubsidiaryState(final String name) {
        this.name = name.toLowerCase();
        this.id = ALL_STATES.size();
        ALL_STATES.add(this);
    }

    public void prepareData() {
        this.name = name.toLowerCase();
        this.id = ALL_STATES.size();
        ALL_STATES.add(this);
        if (showSubsidiaryAtSignal != null) {
            showSubsidiary = Enum.valueOf(ShowSubsidiary.class,
                    showSubsidiaryAtSignal.toUpperCase());
        }
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShowSubsidiary getSubsidiaryShowType() {
        return showSubsidiary;
    }

    public boolean isCountable() {
        return isCountable;
    }

    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) id);
    }

    public static SubsidiaryState of(final ReadBuffer buffer) {
        return ALL_STATES.get(buffer.getByteToUnsignedInt());
    }

    private static final String STATE_ID = "stateID";

    public void writeNBT(final NBTWrapper tag) {
        tag.putInteger(STATE_ID, id);
    }

    public static SubsidiaryState of(final NBTWrapper tag) {
        return ALL_STATES.get(tag.getInteger(STATE_ID));
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

    @Override
    public String toString() {
        return name;
    }
}