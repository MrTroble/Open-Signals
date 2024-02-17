package com.troblecodings.signals.core;

import net.minecraft.util.math.vector.Vector3i;

public final class Vector3iHelper {

    private Vector3iHelper() {
    }

    public static Vector3i subtract(final Vector3i first, final Vector3i second) {
        return new Vector3i(first.getX() - second.getX(), first.getY() - second.getY(),
                first.getZ() - second.getZ());
    }
}
