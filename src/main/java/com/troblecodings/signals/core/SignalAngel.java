package com.troblecodings.signals.core;

import com.troblecodings.core.QuaternionWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum SignalAngel implements NamableWrapper {

    ANGEL0, ANGEL22P5, ANGEL45, ANGEL67P5, ANGEL90, ANGEL112P5, ANGEL135, ANGEL157P5, ANGEL180,
    ANGEL202P5, ANGEL225, ANGEL247P5, ANGEL270, ANGEL292P5, ANGEL315, ANGEL337P5;

    private SignalAngel() {
    }

    public double getRadians() {
        return Math.PI * 2.0 - (this.ordinal() / 16.0) * Math.PI * 2.0;
    }

    @OnlyIn(Dist.CLIENT)
    public Quaternion getQuaternion() {
        return QuaternionWrapper.fromXYZ(0, (float) getRadians(), 0);
    }

    @Override
    public String getNameWrapper() {
        return "angel" + this.ordinal();
    }
}