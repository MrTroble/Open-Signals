package com.troblecodings.signals.blocks;

import java.util.function.Predicate;

import net.minecraft.util.SoundEvent;

@SuppressWarnings("rawtypes")
public class SoundProperty {

    public final SoundEvent sound;
    public final Predicate predicate;
    public final int duration;

    public SoundProperty() {
        this(null, t -> true, -1);
    }

    public SoundProperty(final SoundEvent sound, final Predicate predicate, final int duration) {
        super();
        this.sound = sound;
        this.predicate = predicate;
        this.duration = duration;
    }

}
