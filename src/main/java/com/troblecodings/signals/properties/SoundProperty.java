package com.troblecodings.signals.properties;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.properties.PredicatedPropertyBase.PredicateProperty;

import net.minecraft.util.SoundEvent;

public class SoundProperty extends PredicateProperty<SoundEvent> {

    public final int duration;

    public SoundProperty(final Predicate<Map<SEProperty, String>> predicate, final SoundEvent state, 
            final int duration) {
        super(predicate, state);
        this.duration = duration;
    }
}