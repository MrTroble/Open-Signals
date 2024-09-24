package com.troblecodings.signals.animation;

import java.util.Objects;

import com.mojang.math.Quaternion;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.core.RenderAnimationInfo;

public class ModelTranslation {

    private VectorWrapper firstTranslation;
    private Quaternion quaternion = new Quaternion(0, 0, 0, 0);
    private VectorWrapper lastTranslation;
    private SignalAnimation animation;
    private VectorWrapper modelTranslation = VectorWrapper.ZERO;
    private boolean renderModel = false;

    public ModelTranslation(final VectorWrapper firstTranslation, final Quaternion quaternion,
            final VectorWrapper lastTranslation) {
        this.firstTranslation = firstTranslation;
        this.quaternion = quaternion;
        this.lastTranslation = lastTranslation;
    }

    public void translate(final RenderAnimationInfo info) {
        info.stack.translate(firstTranslation.getX(), firstTranslation.getY(),
                firstTranslation.getZ());
        info.stack.mulPose(quaternion);
        info.stack.translate(lastTranslation.getX(), lastTranslation.getY(),
                lastTranslation.getZ());
        if (!modelTranslation.equals(VectorWrapper.ZERO)) {
            info.stack.translate(modelTranslation.getX(), modelTranslation.getY(),
                    modelTranslation.getZ());
        }
    }

    public Quaternion getQuaternion() {
        return quaternion;
    }

    public boolean shouldRenderModel() {
        return renderModel;
    }

    public ModelTranslation setRenderModel(final boolean renderModel) {
        this.renderModel = renderModel;
        return this;
    }

    public void setUpNewTranslation(final ModelTranslation other) {
        this.firstTranslation = other.firstTranslation;
        this.quaternion = other.quaternion;
        this.lastTranslation = other.lastTranslation;
    }

    public void setModelTranslation(final VectorWrapper translation) {
        this.modelTranslation = translation;
    }

    public void assignAnimation(final SignalAnimation animation) {
        this.animation = animation;
    }

    public void removeAnimation() {
        this.animation = null;
    }

    public boolean isAnimationAssigned() {
        return animation != null;
    }

    public SignalAnimation getAssigendAnimation() {
        return animation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstTranslation, lastTranslation, quaternion, renderModel);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ModelTranslation other = (ModelTranslation) obj;
        return Objects.equals(firstTranslation, other.firstTranslation)
                && Objects.equals(lastTranslation, other.lastTranslation)
                && Objects.equals(quaternion, other.quaternion) && renderModel == other.renderModel;
    }

}
