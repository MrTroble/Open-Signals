package com.troblecodings.signals.animation;

import java.util.Arrays;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.troblecodings.signals.OpenSignalsMain;

public enum RotationAxis {

    X("X"),
    Y("Y"),
    Z("Z");

    private final String axis;

    RotationAxis(final String axis) {
        this.axis = axis;
    }

    public static RotationAxis of(final String axis) {
        return Arrays.stream(values())
                .filter(rotationAxis -> rotationAxis.axis.equalsIgnoreCase(axis))
                .findFirst()
                .orElseGet(() -> {
                    OpenSignalsMain.exitMinecraftWithMessage(
                            "[" + axis + "] is not a valid axis for a RotationAnimation! Valid are: " + Arrays.toString(values()));
                    return null;
                });
    }

    public Quaternionf getForAxis(final float value) {
        return switch (this) {
            case X -> Axis.XP.rotationDegrees(value);
            case Y -> Axis.YP.rotationDegrees(value);
            case Z -> Axis.ZP.rotationDegrees(value);
        };
    }
}