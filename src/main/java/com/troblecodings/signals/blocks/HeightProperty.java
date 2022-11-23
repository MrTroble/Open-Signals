package com.troblecodings.signals.blocks;

import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class HeightProperty {

    public final Predicate predicate;
    public final int height;

    public HeightProperty(Predicate predicate, int height) {
        super();
        this.predicate = predicate;
        this.height = height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, predicate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HeightProperty other = (HeightProperty) obj;
        return height == other.height && Objects.equals(predicate, other.predicate);
    }
}
