package com.troblecodings.properties;

import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class HeightProperty {

    public final Predicate predicate;
    public final int height;

    public HeightProperty(final Predicate predicate, final int height) {
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
        final HeightProperty other = (HeightProperty) obj;
        return height == other.height && Objects.equals(predicate, other.predicate);
    }
}
