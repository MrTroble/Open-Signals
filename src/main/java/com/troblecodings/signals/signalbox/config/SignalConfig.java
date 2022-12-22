package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.ChangeConfigParser;
import com.troblecodings.signals.contentpacks.DefaultConfigParser;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.properties.ConfigProperty;
import com.troblecodings.signals.properties.SignalPair;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public final class SignalConfig {

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.current.getSignal();
        if (info.type.equals(PathType.NORMAL)) {
            if (info.next != null) {
                final Signal nextSignal = info.next.getSignal();
                final SignalPair pair = new SignalPair(currentSignal, nextSignal);
                final List<ConfigProperty> values = ChangeConfigParser.CHANGECONFIGS.get(pair);
                if (values != null) {
                    changeIfPresent(values, info);
                } else {
                    loadDefault(info);
                }
            } else {
                loadDefault(info);
            }
        } else if (info.type.equals(PathType.SHUNTING)) {
            final List<ConfigProperty> shuntingValues = OneSignalConfigParser.SHUNTINGCONFIGS
                    .get(currentSignal);
            if (shuntingValues != null) {
                loadWithoutPredicate(shuntingValues, info.current);
            }
        }
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    private static void loadDefault(final ConfigInfo info) {
        final List<ConfigProperty> defaultValues = DefaultConfigParser.DEFAULTCONFIGS
                .get(info.current.getSignal());
        if (defaultValues != null) {
            final Map<Class, Object> object = new HashMap<>();
            object.put(Integer.class, info.speed);
            defaultValues.forEach(value -> {
                if (value.predicate.test(object)) {
                    value.values.forEach((prop, val) -> {
                        info.current.getProperty(prop)
                                .ifPresent(_u -> info.current.setProperty(prop, val));
                    });
                }
            });
        }
    }

    public static void reset(final SignalTileEnity current) {
        final List<ConfigProperty> resetValues = OneSignalConfigParser.RESETCONFIGS
                .get(current.getSignal());
        if (resetValues != null) {
            loadWithoutPredicate(resetValues, current);
        }
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static void changeIfPresent(final List<ConfigProperty> values, final ConfigInfo info) {
        final Map<Class, Object> object = new HashMap<>();
        object.put(Map.class, info.next.getProperties());
        object.put(Integer.class, info.speed);
        values.forEach(property -> {
            if (property.predicate.test(object)) {
                property.values.forEach((prop, val) -> {
                    info.current.getProperty(prop)
                            .ifPresent(_u -> info.current.setProperty(prop, (Comparable) val));
                });
            }
        });
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static void loadWithoutPredicate(final List<ConfigProperty> values,
            final SignalTileEnity current) {
        if (values != null) {
            values.forEach(property -> {
                property.values.forEach((prop, val) -> {
                    current.getProperty(prop)
                            .ifPresent(_u -> current.setProperty(prop, (Comparable) val));
                });
            });
        }
    }
}
