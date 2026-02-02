package com.troblecodings.signals.animation;

import java.util.Objects;
import java.util.function.Predicate;

import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.models.ModelInfoWrapper;

public class SignalAnimationTranslation implements SignalAnimation {

    private final Predicate<ModelInfoWrapper> predicate;
    private String model;
    private final float animationSpeed;
    private final VectorWrapper dest;

    private float stepX, stepY, stepZ, progressX, progressY, progressZ, maxX, maxY, maxZ;

    private boolean finishedX = false;
    private boolean finishedY = false;
    private boolean finishedZ = false;

    public SignalAnimationTranslation(final Predicate<ModelInfoWrapper> predicate,
            final float animationSpeed, final VectorWrapper dest) {
        this.predicate = predicate;
        this.animationSpeed = animationSpeed;
        this.dest = dest;
    }

    @Override
    public void updateAnimation(final float tick) {
        if (!finishedX) {
            progressX += stepX * tick;
            this.finishedX = isAnimationOnAxisIsFinished(stepX, progressX, maxX);
        }
        if (!finishedY) {
            progressY += stepY * tick;
            this.finishedY = isAnimationOnAxisIsFinished(stepY, progressY, maxY);
        }
        if (!finishedZ) {
            progressZ += stepZ * tick;
            this.finishedZ = isAnimationOnAxisIsFinished(stepZ, progressZ, maxZ);
        }
    }

    @Override
    public void setUpAnimationValues(final ModelTranslation currentTranslation) {
        this.stepX = SignalAnimationHandler.BASIC_ANIMATION_SPEED * animationSpeed;
        this.stepY = SignalAnimationHandler.BASIC_ANIMATION_SPEED * animationSpeed;
        this.stepZ = SignalAnimationHandler.BASIC_ANIMATION_SPEED * animationSpeed;

        final VectorWrapper start = currentTranslation.getTranslation();
        this.progressX = start.getX();
        this.progressY = start.getY();
        this.progressZ = start.getZ();

        this.maxX = dest.getX();
        this.maxY = dest.getY();
        this.maxZ = dest.getZ();

        this.stepX = maxX < progressX ? -stepX : stepX;
        this.stepY = maxY < progressY ? -stepY : stepY;
        this.stepZ = maxZ < progressZ ? -stepZ : stepZ;

        this.finishedX = this.finishedY = this.finishedZ = false;
    }

    @Override
    public ModelTranslation getFinalModelTranslation() {
        return new ModelTranslation(dest);
    }

    @Override
    public ModelTranslation getModelTranslation() {
        return new ModelTranslation(new VectorWrapper(progressX, progressY, progressZ));
    }

    @Override
    public boolean isFinished() {
        return finishedX && finishedY && finishedZ;
    }

    private static boolean isAnimationOnAxisIsFinished(final float step, final float progress,
            final float max) {
        if (step > 0) {
            if (progress < max)
                return false;
        } else if (max < progress)
            return false;
        return true;
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
        return new SignalAnimationTranslation(predicate, animationSpeed, dest);
    }

    @Override
    public Predicate<ModelInfoWrapper> getPredicate() {
        return predicate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(animationSpeed, dest, finishedX, finishedY, finishedZ, maxX, maxY, maxZ,
                model, predicate, progressX, progressY, progressZ, stepX, stepY, stepZ);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalAnimationTranslation other = (SignalAnimationTranslation) obj;
        return Float.floatToIntBits(animationSpeed) == Float.floatToIntBits(other.animationSpeed)
                && Objects.equals(dest, other.dest) && finishedX == other.finishedX
                && finishedY == other.finishedY && finishedZ == other.finishedZ
                && Float.floatToIntBits(maxX) == Float.floatToIntBits(other.maxX)
                && Float.floatToIntBits(maxY) == Float.floatToIntBits(other.maxY)
                && Float.floatToIntBits(maxZ) == Float.floatToIntBits(other.maxZ)
                && Objects.equals(model, other.model) && Objects.equals(predicate, other.predicate)
                && Float.floatToIntBits(progressX) == Float.floatToIntBits(other.progressX)
                && Float.floatToIntBits(progressY) == Float.floatToIntBits(other.progressY)
                && Float.floatToIntBits(progressZ) == Float.floatToIntBits(other.progressZ)
                && Float.floatToIntBits(stepX) == Float.floatToIntBits(other.stepX)
                && Float.floatToIntBits(stepY) == Float.floatToIntBits(other.stepY)
                && Float.floatToIntBits(stepZ) == Float.floatToIntBits(other.stepZ);
    }

}