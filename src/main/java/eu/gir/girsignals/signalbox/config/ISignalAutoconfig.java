package eu.gir.girsignals.signalbox.config;

import java.util.Map;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

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
