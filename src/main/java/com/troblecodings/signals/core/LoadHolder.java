package com.troblecodings.signals.core;

import java.util.Objects;

public class LoadHolder<T> {

    public final T holder;

    public LoadHolder(final T holder) {
        this.holder = holder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LoadHolder<?> other = (LoadHolder<?>) obj;
        return Objects.equals(holder, other.holder);
    }

}