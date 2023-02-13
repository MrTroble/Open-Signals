package com.troblecodings.signals.parser;

import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;

@SuppressWarnings("rawtypes")
public class ValuePack {

    public final SEProperty property;
    public final Predicate predicate;

    public ValuePack(final SEProperty property, final Predicate predicate) {
        super();
        this.property = property;
        this.predicate = predicate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate, property);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final ValuePack other = (ValuePack) obj;
        return Objects.equals(predicate, other.predicate)
                && Objects.equals(property, other.property);
    }

}
