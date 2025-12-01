package com.troblecodings.signals.animation;

import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.core.QuaternionWrapper;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.util.math.vector.Vector3f;

public class SignalAnimationRotation implements SignalAnimation {

    private final Predicate<ModelInfoWrapper> predicate;
    private final float animationSpeed;
    private final RotationAxis axis;
    private final float rotation;
    private final VectorWrapper pivot;
    private final float finalRotationValue;

    private float step;
    private float progress;

    public SignalAnimationRotation(final Predicate<ModelInfoWrapper> predicate,
            final float animationSpeed, final RotationAxis axis, final float rotation,
            final VectorWrapper pivot) {
        this.predicate = predicate;
        this.animationSpeed = animationSpeed;
        this.axis = axis;
        this.rotation = rotation;
        this.pivot = pivot;
        this.finalRotationValue = rotation * 0.005f * 3.49065f;
    }

    @Override
    public void updateAnimation(final float tick) {
        progress += step * tick;
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        final Vector3f vec = QuaternionWrapper.toYXZ(currentTranslation.getQuaternion());
        switch (axis) {
            case X: {
                progress = vec.x();
                break;
            }
            case Y: {
                progress = vec.y();
                break;
            }
            case Z: {
                progress = vec.z();
                break;
            }
            default:
                break;
        }
        this.step = SignalAnimationHandler.BASIC_ANIMATION_SPEED * animationSpeed;
        if (finalRotationValue < progress)
            this.step *= -1;
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(pivot, axis.getForAxis(finalRotationValue));
    }

    @Override
    public ModelTranslation getModelTranslation() {
        return new ModelTranslation(pivot, axis.getForAxis(progress));
    }

    @Override
    public boolean isFinished() {
        return this.step > 0 ? (progress > finalRotationValue) : (finalRotationValue > progress);
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean test(final ModelInfoWrapper wrapper) {
        return predicate.test(wrapper);
    }

    @Override
    public SignalAnimation copy() {
        return new SignalAnimationRotation(predicate, animationSpeed, axis, rotation, pivot);
    }

    @Override
    public Predicate<ModelInfoWrapper> getPredicate() {
        return predicate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, axis, finalRotationValue, pivot, predicate, rotation);
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
                && axis == other.axis
                && Float.floatToIntBits(finalRotationValue) == Float
                        .floatToIntBits(other.finalRotationValue)
                && Objects.equals(pivot, other.pivot) && Objects.equals(predicate, other.predicate)
                && Float.floatToIntBits(rotation) == Float.floatToIntBits(other.rotation);
    }

}