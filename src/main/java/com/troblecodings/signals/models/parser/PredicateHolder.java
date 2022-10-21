package com.troblecodings.signals.models.parser;

import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.DefaultName;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public final class PredicateHolder {

    private PredicateHolder() {
    }

    public static <T extends Enum<?>> Predicate<IExtendedBlockState> has(
            final IUnlistedProperty<T> property) {
        return ebs -> ebs.getValue(property) != null;
    }

    public static <T extends Enum<?>> Predicate<IExtendedBlockState> hasNot(
            final IUnlistedProperty<T> property) {
        return ebs -> ebs.getValue(property) == null;
    }

    @SuppressWarnings("unchecked")
    public static Predicate<IExtendedBlockState> with(final ValuePack pack) {
        return with(pack.property, pack.predicate);
    }

    public static <T extends DefaultName<?>> Predicate<IExtendedBlockState> with(
            final IUnlistedProperty<T> property, final Predicate<T> t) {
        return bs -> {
            final T test = bs.getValue(property);
            return test != null && t.test(test);
        };
    }

    public static Predicate<IExtendedBlockState> hasAndIs(
            final IUnlistedProperty<Boolean> property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && bool.booleanValue();
        };
    }

    public static Predicate<IExtendedBlockState> hasAndIsNot(
            final IUnlistedProperty<Boolean> property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && !bool.booleanValue();
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Predicate<Set<Entry<SEProperty<?>, Object>>> check(
            final ValuePack pack) {
        return check(pack.property, pack.predicate);
    }

    public static <T extends Comparable<T>> Predicate<Set<Entry<SEProperty<?>, Object>>> check(
            final IUnlistedProperty<T> property, final T type) {
        return check(property, type::equals);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> Predicate<Set<Entry<SEProperty<?>, Object>>> check(
            final IUnlistedProperty<T> property, final Predicate<T> type) {
        return t -> t.stream().noneMatch(e -> e.getKey().equals(property)) || t.stream()
                .anyMatch((e -> e.getKey().equals(property) && type.test((T) e.getValue())));
    }

}
