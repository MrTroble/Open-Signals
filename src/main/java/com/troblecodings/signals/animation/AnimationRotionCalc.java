package com.troblecodings.signals.animation;

import java.util.Objects;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public class AnimationRotionCalc {

    private final float animationSpeed;
    private float step;
    private final RotationAxis axis;
    private float progress;
    private float max;

    public AnimationRotionCalc(final Vector3f startPosition, final Vector3f finalPosition,
            final float animationSpeed, final RotationAxis axis) {
        this.animationSpeed = animationSpeed;
        this.step = 0.005f * animationSpeed;
        this.axis = axis;
        calculateWayAndValues(startPosition, finalPosition);
    }

    private void calculateWayAndValues(final Vector3f start, final Vector3f end) {
        switch (axis) {
            case X: {
                progress = start.x();
                max = end.x();
                break;
            }
            case Y: {
                progress = start.y();
                max = end.y();
                break;
            }
            case Z: {
                progress = start.z();
                max = end.z();
                break;
            }
        }
        if (max < progress) {
            this.step = -step;
        }
    }

    public void updateAnimation() {
        progress += step;
    }

    public boolean isAnimationFinished() {
        if (step > 0) {
            if (progress < max) {
                return false;
            }
        } else {
            if (max < progress) {
                return false;
            }
        }
        return true;
    }

    public Quaternion getQuaternion() {
        return axis.getForAxis(progress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, axis, max, progress, step);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final AnimationRotionCalc other = (AnimationRotionCalc) obj;
        return Float.floatToIntBits(animationSpeed) == Float.floatToIntBits(other.animationSpeed)
                && axis == other.axis
                && Float.floatToIntBits(max) == Float.floatToIntBits(other.max)
                && Float.floatToIntBits(progress) == Float.floatToIntBits(other.progress)
                && Float.floatToIntBits(step) == Float.floatToIntBits(other.step);
    }

}