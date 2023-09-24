package com.troblecodings.signals.properties;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;

public class PredicatedPropertyBase<T, P> implements Predicate<P> {

    public final Predicate<P> predicate;
    public final T state;

    public PredicatedPropertyBase(final Predicate<P> predicate, final T state) {
        this.predicate = Objects.requireNonNull(predicate);
        this.state = state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PredicatedPropertyBase<T, P> other = (PredicatedPropertyBase<T, P>) obj;
        return Objects.equals(state, other.state);
    }

    @Override
    public String toString() {
        return "PredicatedProperty [state=" + state + "]";
    }

    @Override
    public boolean test(final P t) {
        return this.predicate.test(t);
    }

    // Well because this language is stupid:
    public static class PredicateProperty<T>
            extends PredicatedPropertyBase<T, Map<SEProperty, String>> {

        public PredicateProperty(final Predicate<Map<SEProperty, String>> predicate,
                final T state) {
            super(predicate, state);
        }
    }

    public static class ConfigProperty
            extends PredicatedPropertyBase<Map<SEProperty, String>, Map<Class<?>, Object>> {

        public ConfigProperty(final Predicate<Map<Class<?>, Object>> predicate,
                final Map<SEProperty, String> state) {
            super(predicate, state);
        }

    }
}