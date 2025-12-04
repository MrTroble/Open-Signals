package com.troblecodings.signals.core;

import com.troblecodings.signals.tileentitys.SignalTileEntity;

public class RenderAnimationInfo {

    public final double x;
    public final double y;
    public final double z;
    public final float partialTicks;
    public SignalTileEntity tileEntity;
    public final float tick;

    public RenderAnimationInfo(final double x, final double y, final double z,
            final float partialTicks, final float tick) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.tick = tick;
    }

    public RenderAnimationInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}
