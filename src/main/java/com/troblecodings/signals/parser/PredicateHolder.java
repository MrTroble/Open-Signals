package com.troblecodings.signals.parser;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.enums.CompareValues;

import net.minecraftforge.common.property.IExtendedBlockState;

public final class PredicateHolder {

    private PredicateHolder() {
    }

    public static Predicate<IExtendedBlockState> has(final SEProperty property) {
        return ebs -> ebs.getValue(property) != null;
    }

    public static Predicate<IExtendedBlockState> hasNot(final SEProperty property) {
        return ebs -> ebs.getValue(property) == null;
    }

    @SuppressWarnings("unchecked")
    public static Predicate<IExtendedBlockState> with(final ValuePack pack) {
        return ebs -> {
            final Object test = ebs.getValue(pack.property);
            return test != null && pack.predicate.test(test);
        };
    }

    public static Predicate<IExtendedBlockState> hasAndIs(final SEProperty property) {
        return ebs -> {
            final Boolean bool = "TRUE".equalsIgnoreCase(ebs.getValue(property));
            return bool.booleanValue();
        };
    }

    public static Predicate<IExtendedBlockState> hasAndIsNot(final SEProperty property) {
        return ebs -> {
            final String cacheString = ebs.getValue(property);
            if (cacheString == null)
                return false;
            final Boolean bool = "TRUE".equalsIgnoreCase(ebs.getValue(property));
            return !bool.booleanValue();
        };
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static Predicate<Map<SEProperty, String>> check(final ValuePack pack) {
        return t -> {
            final String value = t.get(pack.property);
            if (value == null)
                return false;
            return pack.predicate.test(value.toUpperCase());
        };
    }

    @SuppressWarnings("unchecked")
    public static Predicate<Map<SEProperty, String>> config(final ValuePack pack) {
        return t -> {
            final String value = t.get(pack.property);
            if (value == null)
                return false;
            return pack.predicate.test(value.toUpperCase());
        };
    }

    public static Predicate<Integer> speed(final StringInteger stringInt) {
        final CompareValues values = CompareValues.of(stringInt.string);
        if (values == null)
            throw new LogicalParserException("The given operator of the speed function ["
                    + stringInt.string + "] is not permitted!");
        final int speed = stringInt.integer;
        switch (values) {
            case GREATER:
                return s -> {
                    return s > speed;
                };

            case GREATEREQUALS:
                return s -> {
                    return s >= speed;
                };

            case EQUALS:
                return s -> {
                    return s == speed;
                };

            case SMALLEREQUALS:
                return s -> {
                    return s <= speed;
                };

            case SMALLER:
                return s -> {
                    return s < speed;
                };

            case UNEQUALS:
                return s -> {
                    return s != speed;
                };

            default:
                return s -> {
                    return s == speed;
                };
        }

    }

    public static Predicate<String> zs2Value(final String value) {
        return s -> {
            return s.equalsIgnoreCase(value);
        };
    }
}
