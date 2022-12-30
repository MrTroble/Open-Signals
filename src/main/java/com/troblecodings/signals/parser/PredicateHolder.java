package com.troblecodings.signals.parser;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.enums.CompareValues;

import net.minecraftforge.client.model.data.IModelData;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public final class PredicateHolder {

    private PredicateHolder() {
    }

    public static Predicate<IModelData> has(final SEProperty property) {
        return ebs -> ebs.getValue(property) != null;
    }

    public static Predicate<IModelData> hasNot(final SEProperty property) {
        return ebs -> ebs.getValue(property) == null;
    }

    public static Predicate<IModelData> with(final ValuePack pack) {
        return with(pack.property, pack.predicate);
    }

    public static Predicate<IModelData> with(final SEProperty property, final Predicate t) {
        return bs -> {
            final Object test = bs.getValue(property);
            return test != null && t.test(test);
        };
    }

    public static Predicate<IModelData> hasAndIs(final SEProperty property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && bool.booleanValue();
        };
    }

    public static Predicate<IModelData> hasAndIsNot(final SEProperty property) {
        return ebs -> {
            final Boolean bool = ebs.getValue(property);
            return bool != null && !bool.booleanValue();
        };
    }

    public static Predicate<Map<SEProperty, Object>> check(final ValuePack pack) {
        return check(pack.property, pack.predicate);
    }

    public static Predicate<Map<SEProperty, Object>> check(final SEProperty property,
            final Object type) {
        return check(property, type::equals);
    }

    public static Predicate<Map<SEProperty, Object>> check(final SEProperty property,
            final Predicate type) {
        return t -> {
            final Object value = t.get(property);
            if (value == null)
                return true;
            return type.test(value);
        };
    }

    public static Predicate<Map<SEProperty, Object>> config(final ValuePack pack) {
        return t -> {
            final Object value = t.get(pack.property);
            if (value == null)
                return false;
            return pack.predicate.test(value);
        };
    }

    public static Predicate<Integer> speed(final StringInteger stringInt) {
        return speed(stringInt.string, stringInt.integer);
    }

    public static Predicate<Integer> speed(final String compare, final int speed) {

        final CompareValues values = CompareValues.getValuefromString(compare);
        if (values == null)
            throw new LogicalParserException(
                    "The given operator of the speed function (" + compare + ") is not permitted!");
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
