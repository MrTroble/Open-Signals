package com.troblecodings.signals.properties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;

@SuppressWarnings("rawtypes")
public class ConfigProperty {

    public final Predicate predicate;
    public Map<SEProperty, String> values;

    public ConfigProperty(final SEProperty property, final String value) {
        this(t -> true, property, value);
    }

    public ConfigProperty(final Predicate predicate, final SEProperty property,
            final String value) {
        this.predicate = predicate;
        this.values = new HashMap<>();
        this.values.put(property, value);
    }

    public ConfigProperty(final Predicate predicate, final Map<SEProperty, String> values) {
        this.predicate = predicate;
        this.values = values;
    }

}