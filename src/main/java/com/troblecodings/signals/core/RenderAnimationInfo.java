package com.troblecodings.signals.core;

import com.troblecodings.signals.tileentitys.SignalTileEntity;

public class RenderAnimationInfo {

    public final double x;
    public final double y;
    public final double z;
    public final float partialTicks;
    public SignalTileEntity tileEntity;

    public RenderAnimationInfo(final double x, final double y, final double z,
            final float partialTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
    }

    public RenderAnimationInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}
