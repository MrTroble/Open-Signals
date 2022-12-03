package com.troblecodings.signals.test;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public class DummySignal extends SignalTileEnity {

    private final HashMap<SEProperty<?>, Object> seproperty = new HashMap<>();

    @Override
    public Optional<?> getProperty(final SEProperty<?> prop) {
        if (seproperty.containsKey(prop))
            return Optional.of(seproperty.get(prop));
        return Optional.empty();
    }

    @Override
    public <T extends Comparable<T>> void setProperty(final SEProperty<T> prop, final T opt) {
        seproperty.put(prop, opt);
    }

    public DummySignal copy() {
        final DummySignal signal = new DummySignal();
        signal.seproperty.putAll(this.seproperty);
        return signal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seproperty);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DummySignal other = (DummySignal) obj;
        return Objects.equals(seproperty, other.seproperty);
    }

    @Override
    public String toString() {
        return "DummySignal [seproperty=" + seproperty + "]";
    }

    public static class DummyBuilder {

        DummySignal signal;

        public DummyBuilder(final DummySignal signal) {
            this.signal = signal;
        }

        public <T extends Comparable<T>> DummyBuilder of(final SEProperty<T> property,
                final T object) {
            this.signal.setProperty(property, object);
            return this;
        }

        public DummySignal build() {
            return this.signal;
        }

        public static <T extends Comparable<T>> DummyBuilder start(final SEProperty<T> property,
                final T object) {
            final DummySignal signal = new DummySignal();
            final DummyBuilder builder = new DummyBuilder(signal);
            return builder.of(property, object);
        }

    }

}
