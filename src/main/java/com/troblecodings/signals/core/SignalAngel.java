package com.troblecodings.signals.core;

import com.troblecodings.core.interfaces.NamableWrapper;

import net.minecraft.util.math.vector.Quaternion;

public enum SignalAngel implements NamableWrapper {

    ANGEL0, ANGEL22P5, ANGEL45, ANGEL67P5, ANGEL90, ANGEL112P5, ANGEL135, ANGEL157P5, ANGEL180,
    ANGEL202P5, ANGEL225, ANGEL247P5, ANGEL270, ANGEL292P5, ANGEL315, ANGEL337P5;

    private Quaternion quaternion;

    private SignalAngel() {
        quaternion = new Quaternion(0, (float) getRadians(), 0, 1.0F);
    }

    public double getRadians() {
        return Math.PI * 2.0 - (this.ordinal() / 16.0) * Math.PI * 2.0;
    }

    public Quaternion getQuaternion() {
        return this.quaternion;
    }

    @Override
    public String getNameWrapper() {
        return "angel" + this.ordinal();
    }
}