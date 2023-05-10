package com.troblecodings.signals.properties;

import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class BooleanProperty {
    
    public final Predicate predicate;
    public final boolean doubleSided;
    
    public BooleanProperty(final Predicate predicate, final boolean doubleSided) {
        super();
        this.predicate = predicate;
        this.doubleSided = doubleSided;
    }

}
