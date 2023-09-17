package com.troblecodings.signals.core;

import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.gui.FontRenderer;

public class RenderOverlayInfo {

    public final double x;
    public final double y;
    public final double z;
    public SignalTileEntity tileEntity;
    public final FontRenderer font;

    public RenderOverlayInfo(final double x, final double y, final double z,
            final FontRenderer fontRenderer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = fontRenderer;
    }

    public RenderOverlayInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}