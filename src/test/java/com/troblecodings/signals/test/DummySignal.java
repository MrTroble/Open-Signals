package com.troblecodings.signals.test;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

public class DummySignal extends SignalTileEntity {

    public DummySignal() {
        super(null);
    }

    private final HashMap<SEProperty, String> seproperty = new HashMap<>();

    @Override
    public Optional<String> getProperty(final SEProperty prop) {
        if (seproperty.containsKey(prop))
            return Optional.of(seproperty.get(prop));
        return Optional.empty();
    }

    @Override
    public void setProperty(final SEProperty prop, final String opt) {
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

        public DummyBuilder of(final SEProperty property, final String object) {
            this.signal.setProperty(property, object);
            return this;
        }

        public DummySignal build() {
            return this.signal;
        }

        public static DummyBuilder start(final SEProperty property, final String object) {
            final DummySignal signal = new DummySignal();
            final DummyBuilder builder = new DummyBuilder(signal);
            return builder.of(property, object);
        }

    }

}
