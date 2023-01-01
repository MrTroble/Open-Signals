package com.troblecodings.signals.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.gui.Font;

public class RenderOverlayInfo {
    public PoseStack stack;
    public double x;
    public double y;
    public double z;
    public SignalTileEntity tileEntity;
    public Font font;

    public RenderOverlayInfo(final PoseStack stack, final double x, final double y, final double z,
            final Font font) {
        super();
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = font;
    }

    public RenderOverlayInfo with(SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}