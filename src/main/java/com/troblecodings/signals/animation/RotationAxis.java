package com.troblecodings.signals.animation;

import java.util.Arrays;

public enum RotationAxis {

    X("X"), Y("Y"), Z("Z");

    private final String axis;

    private RotationAxis(final String axis) {
        this.axis = axis;
    }

    public static RotationAxis of(final String axis) {
        return Arrays.stream(values())
                .filter(rotationAxis -> rotationAxis.axis.equalsIgnoreCase(axis)).findFirst()
                .orElse(null);
    }

}