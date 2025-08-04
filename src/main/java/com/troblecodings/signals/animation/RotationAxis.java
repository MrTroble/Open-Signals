package com.troblecodings.signals.animation;

import java.util.Arrays;

import com.troblecodings.core.QuaternionWrapper;
import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.util.math.vector.Quaternion;

public enum RotationAxis {

    X("X"), Y("Y"), Z("Z");

    private final String axis;

    private RotationAxis(final String axis) {
        this.axis = axis;
    }

    public static RotationAxis of(final String axis) {
        return Arrays.stream(values())
                .filter(rotationAxis -> rotationAxis.axis.equalsIgnoreCase(axis)).findFirst()
                .orElseGet(() -> {
                    OpenSignalsMain.exitMinecraftWithMessage("[" + axis
                            + "] is not a valid axis for a RotationAnimation! Valid are: "
                            + values());
                    return null;
                });
    }

    public Quaternion getForAxis(final float value) {
        switch (this) {
            case X:
                return QuaternionWrapper.fromXYZ(value, 0, 0);
            case Y:
                return QuaternionWrapper.fromXYZ(0, value, 0);
            case Z:
                return QuaternionWrapper.fromXYZ(0, 0, value);
            default:
                return QuaternionWrapper.fromXYZ(0, 0, 0);
        }
    }

}