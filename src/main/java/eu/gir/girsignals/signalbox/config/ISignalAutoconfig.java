package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;

import javax.annotation.Nullable;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public interface ISignalAutoconfig {

    void change(final int speed, final SignalTileEnity current,
            @Nullable final SignalTileEnity next);

    void reset(final SignalTileEnity current);

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    default void changeIfPresent(final HashMap<SEProperty, Object> values,
            final SignalTileEnity current) {
        values.forEach((sep, value) -> current.getProperty(sep)
                .ifPresent(_u -> current.setProperty(sep, (Comparable) value)));
    }
}
