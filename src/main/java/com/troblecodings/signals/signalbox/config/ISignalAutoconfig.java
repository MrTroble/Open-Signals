package com.troblecodings.signals.signalbox.config;

import java.util.Map;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public interface ISignalAutoconfig {

    public void change(final ConfigInfo info);

    public void reset(final SignalTileEnity current);

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    default void changeIfPresent(final Map<SEProperty, Object> values,
            final SignalTileEnity current) {
        values.forEach((sep, value) -> current.getProperty(sep)
                .ifPresent(_u -> current.setProperty(sep, (Comparable) value)));
    }

}
