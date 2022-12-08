package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.contentpacks.TwoSignalConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public final class SignalConfig {

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.current.getSignal();
        if (info.type.equals(PathType.NORMAL)) {
            if (info.next != null) {
                final Signal nextSignal = info.next.getSignal();
                final SignalPair pair = new SignalPair(currentSignal, nextSignal);
                final List<ConfigProperty> values = TwoSignalConfigParser.CHANGECONFIGS.get(pair);
                if (values != null && info.next != null) {
                    changeIfPresent(values, info);
                } else {
                    loadDefault(info.current);
                }
            } else {
                loadDefault(info.current);
            }
        } else if (info.type.equals(PathType.SHUNTING)) {
            final List<ConfigProperty> shuntingValues = OneSignalConfigParser.SHUNTINGCONFIGS
                    .get(currentSignal);
            if (shuntingValues != null) {
                loadIfNextIsNull(shuntingValues, info.current);
            }
        }
    }

    private static void loadDefault(final SignalTileEnity current) {
        final List<ConfigProperty> defaultValues = OneSignalConfigParser.DEFAULTCONFIGS
                .get(current.getSignal());
        if (defaultValues != null) {
            loadIfNextIsNull(defaultValues, current);
        }
    }

    public static void reset(final SignalTileEnity current) {
        final List<ConfigProperty> resetValues = OneSignalConfigParser.RESETCONFIGS
                .get(current.getSignal());
        if (resetValues != null) {
            loadIfNextIsNull(resetValues, current);
        }
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static void changeIfPresent(final List<ConfigProperty> values, final ConfigInfo info) {
        if (info.next == null) {
            loadIfNextIsNull(values, info.current);
        }
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
    private static void loadIfNextIsNull(final List<ConfigProperty> values,
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
