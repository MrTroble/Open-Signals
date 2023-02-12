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

    public static Predicate<ModelInfoWrapper> with(final ValuePack pack) {
        return with(pack.property, pack.predicate);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public static Predicate<ModelInfoWrapper> with(final SEProperty property, final Predicate t) {
        return ebs -> {
            final Object test = ebs.get(property);
            return test != null && t.test(test);
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

    public static Predicate<Map<SEProperty, String>> check(final ValuePack pack) {
        return check(pack.property, pack.predicate);
    }

    public static Predicate<Map<SEProperty, String>> check(final SEProperty property,
            final String type) {
        return check(property, type::equals);
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public static Predicate<Map<SEProperty, String>> check(final SEProperty property,
            final Predicate type) {
        return t -> {
            final String value = t.get(property);
            if (value == null)
                return false;
            return type.test(value.toUpperCase());
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
        return speed(stringInt.string, stringInt.integer);
    }

    public static Predicate<Integer> speed(final String compare, final int speed) {

        final CompareValues values = CompareValues.getValuefromString(compare);
        if (values == null)
            throw new LogicalParserException(
                    "The given operator of the speed function [" + compare + "] is not permitted!");
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
}
