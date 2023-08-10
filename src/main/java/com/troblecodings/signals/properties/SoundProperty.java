package com.troblecodings.signals.properties;

import java.util.function.Predicate;

import net.minecraft.sounds.SoundEvent;

@SuppressWarnings("rawtypes")
public class SoundProperty {

    public final SoundEvent sound;
    public final Predicate predicate;
    public final int duration;

    public SoundProperty() {
        this(null, t -> true, -1);
    }

    public SoundProperty(final SoundEvent sound, final Predicate predicate, final int duration) {
        this.sound = sound;
        this.predicate = predicate;
        this.duration = duration;
    }
}