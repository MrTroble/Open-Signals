package com.troblecodings.signals.models;

import java.util.Objects;
import java.util.function.Predicate;

public class ImplAutoBlockStatePredicate implements Predicate<ModelInfoWrapper> {

    private final int id;

    private static int counter = 0;

    public ImplAutoBlockStatePredicate() {
        this.id = counter++;
    }

    @Override
    public boolean test(final ModelInfoWrapper t) {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ImplAutoBlockStatePredicate other = (ImplAutoBlockStatePredicate) obj;
        return id == other.id;
    }
}