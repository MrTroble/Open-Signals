package com.troblecodings.signals.animation;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.SEProperty;

public class SignalAnimationRotation implements SignalAnimationState {

    private float progress = 0;
    private double max = 0;
    private float step = 0;

    private final Predicate<Map<SEProperty, String>> predicate;
    private String model;
    private final float animationSpeed;
    private final RotationAxis axis;
    private final float rotation;
    private final VectorWrapper pivot;

    private boolean calculatePlus = true;

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
        if (calculatePlus)
            ++progress;
        else
            --progress;
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        final Vector3f vec = currentTranslation.getQuaternion().toXYZ();
        final Vector3f maxPos = new Vector3f(0, 0, -rotation * 0.005f * animationSpeed);
        final float d = (float) (distanceBetween(vec, maxPos) / (0.005f * animationSpeed));
        max = d;
        if (rotation > 0) {
            step = 0.005f * animationSpeed;
        } else if (rotation < 0) {
            step = -0.005f * animationSpeed;
        } else {
            step = -0.005f * animationSpeed;
            max = 0;
            progress = d;
            calculatePlus = false;
        }
        System.out.println();
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(pivot,
                Quaternion.fromXYZ(0, 0, -rotation * 0.005f * animationSpeed), pivot);
    }

    @Override
    public ModelTranslation getModelTranslation() {
        // TODO Apply Axis
        return new ModelTranslation(pivot, Quaternion.fromYXZ(0, 0, -progress * step), pivot);
    }

    @Override
    public boolean isFinished() {
        if (calculatePlus) {
            if (progress < max) {
                return false;
            }
        } else {
            if (progress > max) {
                return false;
            }
        }
        progress = 0;
        calculatePlus = true;
        return true;
    }

    @Override
    public boolean test(final Map<SEProperty, String> properties) {
        return predicate.test(properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, axis, model, pivot, progress, rotation);
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
                && Objects.equals(pivot, other.pivot)
                && Float.floatToIntBits(progress) == Float.floatToIntBits(other.progress)
                && Objects.equals(rotation, other.rotation);
    }

    private static double distanceBetween(final Vector3f vec1, final Vector3f vec2) {
        final float dx = vec2.x() - vec1.x();
        final float dy = vec2.y() - vec1.y();
        final float dz = vec2.z() - vec1.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}