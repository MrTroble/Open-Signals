package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.SEProperty;

public class SignalAnimationTranslation implements SignalAnimation {

    private float progress;

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
    public SignalAnimation with(final String model) {
        this.model = model;
        return this;
    }

    @Override
    public void updateAnimation() {
        // TODO Auto-generated method stub

    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ModelTranslation getModelTranslation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setUpAnimationValues(ModelTranslation currentTranslation) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isFinished() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean test(Map<SEProperty, String> properties) {
        return predicate.test(properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, dest, model, progress);
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
                && Objects.equals(dest, other.dest) && Objects.equals(model, other.model)
                && Float.floatToIntBits(progress) == Float.floatToIntBits(other.progress);
    }

}