package com.troblecodings.signals.parser;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings("rawtypes")
public class ValuePack {

    public final IUnlistedProperty property;
    public final Predicate predicate;

    public ValuePack(final IUnlistedProperty property, final Predicate predicate) {
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
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ValuePack other = (ValuePack) obj;
        return Objects.equals(predicate, other.predicate)
                && Objects.equals(property, other.property);
    }

}
