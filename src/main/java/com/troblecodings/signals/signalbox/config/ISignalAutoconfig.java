package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.List;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.contentpacks.SignalConfigParser;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public class ISignalAutoconfig {

    @SuppressWarnings("unchecked")
    public static void change(final ConfigInfo info) {
        final SignalPair pair = new SignalPair(info.current.getSignal(), info.next.getSignal());
        final List<ConfigProperty> values = SignalConfigParser.SIGNALCONFIGS.get(pair);
        if (values != null) {
            values.forEach(value -> {
                if (value.predicate.test(info)) {

                }
            });
        } else {
        }
    }

    public static void reset(final SignalTileEnity current) {

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
