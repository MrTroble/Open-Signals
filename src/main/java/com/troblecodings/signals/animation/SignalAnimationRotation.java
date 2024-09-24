package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.SEProperty;

public class SignalAnimationRotation implements SignalAnimationState {

    private AnimationRotionCalc calc;

    private final Predicate<Map<SEProperty, String>> predicate;
    private String model;
    private final float animationSpeed;
    private final RotationAxis axis;
    private final float rotation;
    private final VectorWrapper pivot;

    public SignalAnimationRotation(final Predicate<Map<SEProperty, String>> predicate,
            final float animationSpeed, final RotationAxis axis, final float rotation,
            final VectorWrapper pivot) {
        this.predicate = predicate;
        this.animationSpeed = animationSpeed;
        this.axis = axis;
        this.rotation = rotation;
        this.pivot = pivot;
    }

    @Override
    public SignalAnimationState with(final String model) {
        this.model = model;
        return this;
    }

    @Override
    public void updateAnimation() {
        calc.updateAnimation();
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        final Vector3f vec = currentTranslation.getQuaternion().toXYZ();
        final Vector3f maxPos = new Vector3f(0, 0, -rotation * 0.005f * animationSpeed);
        this.calc = new AnimationRotionCalc(vec, maxPos, animationSpeed);
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(pivot,
                Quaternion.fromXYZ(0, 0, -rotation * 0.005f * animationSpeed), pivot);
    }

    @Override
    public ModelTranslation getModelTranslation() {
        // TODO Apply Axis
        return new ModelTranslation(pivot, calc.getQuaternion(), pivot);
    }

    @Override
    public boolean isFinished() {
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
        return Objects.hash(animationSpeed, axis, model, pivot, calc, rotation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalAnimationRotation other = (SignalAnimationRotation) obj;
        return Float.floatToIntBits(animationSpeed) == Float.floatToIntBits(other.animationSpeed)
                && axis == other.axis && Objects.equals(model, other.model)
                && Objects.equals(pivot, other.pivot) && Objects.equals(calc, other.calc)
                && Objects.equals(rotation, other.rotation);
    }

}