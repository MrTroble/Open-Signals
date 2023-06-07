package com.troblecodings.signals.models;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;

public class ImplAutoBlockStatePredicate implements Predicate<IExtendedBlockState> {

    private final int id;

    private static int counter = 0;

    public ImplAutoBlockStatePredicate() {
        this.id = counter++;
    }

    @Override
    public boolean test(IExtendedBlockState t) {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImplAutoBlockStatePredicate other = (ImplAutoBlockStatePredicate) obj;
        return id == other.id;
    }
}