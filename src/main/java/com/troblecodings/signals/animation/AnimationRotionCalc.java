package com.troblecodings.signals.animation;

import java.util.Objects;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public class AnimationRotionCalc {

    private final float animationSpeed;
    private final float step;
    private float progress;
    private float max;
    private boolean calculatePlus = true;
    private Quaternion quaternion;

    public AnimationRotionCalc(final Vector3f startPosition, final Vector3f finalPosition,
            final float animationSpeed) {
        this.animationSpeed = animationSpeed;
        this.step = 0.005f * animationSpeed;
        calculateWayAndValues(startPosition, finalPosition);
    }

    private void calculateWayAndValues(final Vector3f start, final Vector3f end) {
        progress = start.z();
        max = end.z();
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
        return Quaternion.fromXYZ(0, 0, progress);
    }

    public static double distanceBetween(final Vector3f vec1, final Vector3f vec2) {
        final float dx = vec2.x() - vec1.x();
        final float dy = vec2.y() - vec1.y();
        final float dz = vec2.z() - vec1.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
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