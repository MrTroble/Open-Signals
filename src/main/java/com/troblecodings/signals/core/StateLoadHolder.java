package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.handler.SignalStateInfo;

public class StateLoadHolder {

    public final SignalStateInfo info;
    public final LoadHolder<?> holder;

    public StateLoadHolder(final SignalStateInfo info, final LoadHolder<?> holder) {
        this.info = info;
        this.holder = holder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, info);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StateLoadHolder other = (StateLoadHolder) obj;
        return Objects.equals(holder, other.holder) && Objects.equals(info, other.info);
    }

}