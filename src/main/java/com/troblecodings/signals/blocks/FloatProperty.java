package com.troblecodings.signals.blocks;

import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class FloatProperty {

    public final Predicate predicate;
    public final float height;

    public FloatProperty(final Predicate predicate, final float height) {
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
        final FloatProperty other = (FloatProperty) obj;
        return Float.floatToIntBits(height) == Float.floatToIntBits(other.height)
                && Objects.equals(predicate, other.predicate);
    }

}