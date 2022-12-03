package com.troblecodings.signals.signalbox.config;

import java.util.List;

import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.contentpacks.TwoSignalConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public class ISignalConfig {

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.current.getSignal();
        if (info.type.equals(PathType.NORMAL)) {
            if (info.next != null) {
                final Signal nextSignal = info.next.getSignal();
                final SignalPair pair = new SignalPair(currentSignal, nextSignal);
                final List<ConfigProperty> values = TwoSignalConfigParser.CHANGECONFIGS.get(pair);
                if (values != null) {
                    changeIfPresent(values, info.current);
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
                changeIfPresent(shuntingValues, info.current);
            }
        }
    }

    private static void loadDefault(final SignalTileEnity currentSignal) {
        final List<ConfigProperty> defaultValues = OneSignalConfigParser.DEFAULTCONFIGS
                .get(currentSignal.getSignal());
        if (defaultValues != null) {
            changeIfPresent(defaultValues, currentSignal);
        }
    }

    public static void reset(final SignalTileEnity current) {
        final List<ConfigProperty> resetValues = OneSignalConfigParser.RESETCONFIGS
                .get(current.getSignal());
        if (resetValues != null) {
            changeIfPresent(resetValues, current);
        }
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static void changeIfPresent(final List<ConfigProperty> values,
            final SignalTileEnity current) {
        values.forEach(property -> {
            if (property.predicate.test(current.getProperties())) {
                current.getProperty(property.property).ifPresent(
                        _u -> current.setProperty(property.property, (Comparable) property.value));
            }
        });
    }
}
