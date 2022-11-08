package com.troblecodings.signals.models.parser;

import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public final class PredicateHolder {

    private PredicateHolder() {
    }

    public static Predicate<IExtendedBlockState> has(final IUnlistedProperty property) {
        return ebs -> ebs.getValue(property) != null;
    }

    public static Predicate<IExtendedBlockState> hasNot(final IUnlistedProperty property) {
        return ebs -> ebs.getValue(property) == null;
    }

    public static Predicate<IExtendedBlockState> with(final ValuePack pack) {
        return with(pack.property, pack.predicate);
    }

    public static Predicate<IExtendedBlockState> with(final IUnlistedProperty property,
            final Predicate t) {
        return bs -> {
            final Object test = bs.getValue(property);
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

    public static Predicate<Set<Entry<SEProperty<?>, Object>>> check(final ValuePack pack) {
        return check(pack.property, pack.predicate);
    }

    public static Predicate<Set<Entry<SEProperty<?>, Object>>> check(
            final IUnlistedProperty property, final Object type) {
        return check(property, type::equals);
    }

    public static Predicate<Set<Entry<SEProperty<?>, Object>>> check(
            final IUnlistedProperty property, final Predicate type) {
        return t -> t.stream().noneMatch(e -> e.getKey().equals(property)) || t.stream()
                .anyMatch((e -> e.getKey().equals(property) && type.test(e.getValue())));
    }

}
