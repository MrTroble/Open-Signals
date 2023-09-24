package com.troblecodings.signals.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.gui.Font;

public class RenderOverlayInfo {

    public final PoseStack stack;
    public final double x;
    public final double y;
    public final double z;
    public SignalTileEntity tileEntity;
    public final Font font;

    public RenderOverlayInfo(final PoseStack stack, final double x, final double y, final double z,
            final Font font) {
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = font;
    }

    public RenderOverlayInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}