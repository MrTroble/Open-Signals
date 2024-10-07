package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.SEProperty;

public class SignalAnimationTranslation implements SignalAnimation {

    private AnimationTranslationCalc calc;

    private final Predicate<Map<SEProperty, String>> predicate;
    private String model;
    private final float animationSpeed;
    private final VectorWrapper dest;

    public SignalAnimationTranslation(Predicate<Map<SEProperty, String>> predicate,
            final float animationSpeed, final VectorWrapper dest) {
        this.predicate = predicate;
        this.animationSpeed = animationSpeed;
        this.dest = dest;
    }

    @Override
    public void updateAnimation() {
        calc.updateAnimation();
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        this.calc = new AnimationTranslationCalc(currentTranslation.getTranslation(), dest,
                animationSpeed);
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(dest);
    }

    @Override
    public ModelTranslation getModelTranslation() {
        return new ModelTranslation(calc.getTranslation());
    }

    @Override
    public boolean isFinished() {
        if (calc == null)
            return true;
        return calc.isAnimationFinished();
    }

    @Override
    public void reset() {
        calc = null;
    }

    @Override
    public boolean test(final Map<SEProperty, String> properties) {
        return predicate.test(properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, dest, model);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalAnimationTranslation other = (SignalAnimationTranslation) obj;
        return Float.floatToIntBits(animationSpeed) == Float.floatToIntBits(other.animationSpeed)
                && Objects.equals(dest, other.dest) && Objects.equals(model, other.model);
    }

}