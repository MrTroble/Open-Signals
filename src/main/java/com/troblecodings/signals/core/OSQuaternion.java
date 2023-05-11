package com.troblecodings.signals.core;

import net.minecraft.util.math.vector.Quaternion;

public class OSQuaternion {
    
    public static Quaternion fromXYZ(final float p_175229_, final float p_175230_, final float p_175231_) {
        final Quaternion quaternion = Quaternion.ONE.copy();
        quaternion.mul(new Quaternion((float)Math.sin(p_175229_ / 2.0F), 0.0F, 0.0F, (float)Math.cos(p_175229_ / 2.0F)));
        quaternion.mul(new Quaternion(0.0F, (float)Math.sin(p_175230_ / 2.0F), 0.0F, (float)Math.cos(p_175230_ / 2.0F)));
        quaternion.mul(new Quaternion(0.0F, 0.0F, (float)Math.sin(p_175231_ / 2.0F), (float)Math.cos(p_175231_ / 2.0F)));
        return quaternion;
     }

}
