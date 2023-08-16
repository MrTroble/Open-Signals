package com.troblecodings.signals.properties;

import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class BooleanProperty {

    public final Predicate predicate;
    public final boolean state;

    public BooleanProperty(final Predicate predicate, final boolean state) {
        this.predicate = predicate;
        this.state = state;
    }
}