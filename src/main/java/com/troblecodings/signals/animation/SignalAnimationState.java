package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.function.Predicate;

import com.troblecodings.signals.SEProperty;

public interface SignalAnimationState extends Predicate<Map<SEProperty, String>> {

    public SignalAnimationState with(final String model);

    public void updateAnimation();

    public ModelTranslation getModelTranslation();

    public ModelTranslation getFinalModelTranslation();

    public boolean isFinished();

    public void reset();

    public void setUpAnimationValues(final ModelTranslation currentTranslation);

}