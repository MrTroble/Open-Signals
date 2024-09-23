package com.troblecodings.signals.animation;

import com.mojang.math.Quaternion;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.core.RenderAnimationInfo;

public class ModelTranslation {

    private VectorWrapper firstTranslation;
    private Quaternion quaternion = new Quaternion(0, 0, 0, 0);
    private VectorWrapper lastTranslation;
    private SignalAnimationState animation;
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

    public void assignAnimation(final SignalAnimationState animation) {
        this.animation = animation;
    }

    public void removeAnimation() {
        this.animation = null;
    }

    public boolean isAnimationAssigned() {
        return animation != null;
    }

    public SignalAnimationState getAssigendAnimation() {
        return animation;
    }

}
