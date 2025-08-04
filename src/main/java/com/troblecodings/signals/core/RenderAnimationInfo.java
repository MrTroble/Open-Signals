package com.troblecodings.signals.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderAnimationInfo {

    public final MatrixStack stack;
    public SignalTileEntity tileEntity;
    public final BlockRendererDispatcher dispatcher;
    public final IRenderTypeBuffer source;
    public final int lightColor;
    public final int overlayTexture;

    public RenderAnimationInfo(final MatrixStack stack, final BlockRendererDispatcher dispatcher,
            final IRenderTypeBuffer source, final int lightColor, final int overlayTexture) {
        this.stack = stack;
        this.dispatcher = dispatcher;
        this.source = source;
        this.lightColor = lightColor;
        this.overlayTexture = overlayTexture;
    }

    public RenderAnimationInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }

}