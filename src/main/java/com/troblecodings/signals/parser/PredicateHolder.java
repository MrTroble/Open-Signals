package com.troblecodings.signals.parser;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.enums.CompareValues;
import com.troblecodings.signals.models.ModelInfoWrapper;

public final class PredicateHolder {

    private PredicateHolder() {
    }

    public static Predicate<ModelInfoWrapper> has(final SEProperty property) {
        return ebs -> ebs.get(property) != null;
    }

    public static Predicate<ModelInfoWrapper> hasNot(final SEProperty property) {
        return ebs -> ebs.get(property) == null;
    }

    @SuppressWarnings("unchecked")
    public static Predicate<ModelInfoWrapper> with(final ValuePack pack) {
        return ebs -> {
            final Object test = ebs.get(pack.property);
            return test != null && pack.predicate.test(test);
        };
    }

    public static Predicate<ModelInfoWrapper> hasAndIs(final SEProperty property) {
        return ebs -> {
            final Boolean bool = "TRUE".equalsIgnoreCase(ebs.get(property));
            return bool.booleanValue();
        };
    }

    public static Predicate<ModelInfoWrapper> hasAndIsNot(final SEProperty property) {
        return ebs -> {
            final String cacheString = ebs.get(property);
            if (cacheString == null)
                return false;
            final Boolean bool = "TRUE".equalsIgnoreCase(ebs.get(property));
            return !bool.booleanValue();
        };
    }

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
