package com.troblecodings.signals.animation;

import java.util.Objects;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public class AnimationRotionCalc {

    private final float animationSpeed;
    private final float step;
    private final RotationAxis axis;
    private float progress;
    private float max;
    private boolean calculatePlus = true;
    private Quaternion quaternion;

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
            calculatePlus = false;
        }
    }

    public void updateAnimation() {
        if (calculatePlus) {
            progress += step;
        } else {
            progress -= step;
        }

    }

    public boolean isAnimationFinished() {
        if (calculatePlus) {
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
        return Objects.hash(animationSpeed, quaternion);
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
                && Objects.equals(quaternion, other.quaternion);
    }

}