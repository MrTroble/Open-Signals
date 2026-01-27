package com.troblecodings.signals.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;

import net.minecraft.client.gui.Font;

public class RenderOverlayInfo extends DrawInfo {

    public final double x;
    public final double y;
    public final double z;
    public BasicBlockEntity tileEntity;
    public final Font font;

    public RenderOverlayInfo(final PoseStack stack, final double x, final double y, final double z,
            final Font font) {
        super(stack);
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = font;
    }

    public RenderOverlayInfo with(final BasicBlockEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}