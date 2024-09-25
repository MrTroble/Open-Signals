package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.math.Vector3f;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.SEProperty;

public class SignalAnimationRotation implements SignalAnimation {

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
    public SignalAnimation with(final String model) {
        this.model = model;
        return this;
    }

    @Override
    public void updateAnimation() {
        calc.updateAnimation();
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        final Vector3f vec = currentTranslation.getQuaternion().toYXZ();
        Vector3f maxPos = new Vector3f(0, 0, 0);
        switch (axis) {
            case X: {
                maxPos = new Vector3f(-rotation * 0.005f * animationSpeed, 0, 0);
                break;
            }
            case Y: {
                maxPos = new Vector3f(0, -rotation * 0.005f * animationSpeed, 0);
                break;
            }
            case Z: {
                maxPos = new Vector3f(0, 0, -rotation * 0.005f * animationSpeed);
                break;
            }
            default:
                break;
        }
        this.calc = new AnimationRotionCalc(vec, maxPos, animationSpeed, axis);
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(pivot, axis.getForAxis(-rotation * 0.005f * animationSpeed));
    }

    @Override
    public ModelTranslation getModelTranslation() {
        return new ModelTranslation(pivot, calc.getQuaternion());
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