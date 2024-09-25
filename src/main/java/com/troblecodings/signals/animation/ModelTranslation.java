package com.troblecodings.signals.animation;

import java.util.Objects;

import com.mojang.math.Quaternion;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.core.RenderAnimationInfo;

public class ModelTranslation {

    private VectorWrapper pivotTranslation;
    private Quaternion quaternion = new Quaternion(0, 0, 0, 0);
    private SignalAnimation animation;
    private VectorWrapper modelTranslation = VectorWrapper.ZERO;
    private boolean renderModel = false;

    public ModelTranslation(final VectorWrapper firstTranslation, final Quaternion quaternion) {
        this.pivotTranslation = firstTranslation;
        this.quaternion = quaternion;
    }

    public void translate(final RenderAnimationInfo info) {

        if (!modelTranslation.equals(VectorWrapper.ZERO)) {
            info.stack.translate(modelTranslation.getX(), modelTranslation.getY(),
                    modelTranslation.getZ()); // Modell verschieben
        }
        info.stack.mulPose(quaternion);
        info.stack.translate(pivotTranslation.getX(), pivotTranslation.getY(),
                pivotTranslation.getZ()); // Pivot Punkt
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
        this.pivotTranslation = other.pivotTranslation;
        this.quaternion = other.quaternion;
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
        return Objects.hash(pivotTranslation, quaternion, renderModel);
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
        return Objects.equals(pivotTranslation, other.pivotTranslation)
                && Objects.equals(quaternion, other.quaternion) && renderModel == other.renderModel;
    }

}
