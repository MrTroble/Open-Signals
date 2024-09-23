package com.troblecodings.signals.contentpacks;

import com.troblecodings.core.VectorWrapper;
import com.troblecodings.signals.animation.AnimationMode;
import com.troblecodings.signals.animation.RotationAxis;
import com.troblecodings.signals.animation.SignalAnimationState;
import com.troblecodings.signals.animation.SignalAnimationRotation;
import com.troblecodings.signals.animation.SignalAnimationTranslation;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;

public class SignalAnimationConfig {

    private String predicate;
    private float animationSpeed = 1.0f;
    private String mode;
    private String rotationAxis;
    private float rotation;
    private float pivotX;
    private float pivotY;
    private float pivotZ;

    private float destX;
    private float destY;
    private float destZ;

    public SignalAnimationState createAnimation(final FunctionParsingInfo info) {
        final AnimationMode mode = AnimationMode.of(this.mode);
        switch (mode) {
            case ROTATION: {
                final RotationAxis axis = RotationAxis.of(rotationAxis);
                return new SignalAnimationRotation(LogicParser.predicate(predicate, info),
                        animationSpeed, axis, rotation, new VectorWrapper(pivotX, pivotY, pivotZ));
            }
            case TRANSLATION: {
                return new SignalAnimationTranslation(LogicParser.predicate(predicate, info),
                        animationSpeed, new VectorWrapper(destX, destY, destZ));
            }
            default:
                return null;
        }
    }

}