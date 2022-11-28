package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.contentpacks.OneSignalConfigParser;
import com.troblecodings.signals.contentpacks.TwoSignalConfigParser;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public class ISignalAutoconfig {

    public static void change(final ConfigInfo info) {
        final Signal currentSignal = info.current.getSignal();
        if (info.type.equals(PathType.NORMAL)) {
            if (info.next.getSignal() != null) {
                final SignalPair pair = new SignalPair(currentSignal, info.next.getSignal());
                final List<ConfigProperty> values = TwoSignalConfigParser.CHANGECONFIGS.get(pair);
                if (values != null) {
                    // here needs to be code to check the Predicate of each value and to execute it.
                } else {
                    loadDefault(currentSignal);
                }
            } else {
                loadDefault(currentSignal);
            }
        } else if (info.type.equals(PathType.SHUNTING)) {
            final List<ConfigProperty> shuntingValues = OneSignalConfigParser.SHUNTINGCONFIGS
                    .get(currentSignal);
            if (shuntingValues != null) {
                // here needs to be code to check the Predicate of each value and to execute it.
            }
        }
    }

    private static void loadDefault(final Signal currentSignal) {
        final List<ConfigProperty> defaultValues = OneSignalConfigParser.DEFAULTCONFIGS
                .get(currentSignal);
        if (defaultValues != null) {
            // here needs to be code to check the Predicate of each value and to execute it.
        }
    }

    public static void reset(final SignalTileEnity current) {
        final List<ConfigProperty> resetValues = OneSignalConfigParser.RESETCONFIGS
                .get(current.getSignal());
        if (resetValues != null) {
            // here needs to be code to check the Predicate of each value and to execute it.
        }
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    public static void changeIfPresent(final HashMap<SEProperty, Object> values,
            final SignalTileEnity current) {
        values.forEach((sep, value) -> current.getProperty(sep)
                .ifPresent(_u -> current.setProperty(sep, (Comparable) value)));
    }
}
