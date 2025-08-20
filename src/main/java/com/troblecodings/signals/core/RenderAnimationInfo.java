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
    public final float tick;

    public RenderAnimationInfo(final PoseStack stack, final BlockRenderDispatcher dispatcher,
            final MultiBufferSource source, final int lightColor, final int overlayTexture, float tick) {
        this.stack = stack;
        this.dispatcher = dispatcher;
        this.source = source;
        this.lightColor = lightColor;
        this.overlayTexture = overlayTexture;
		this.tick = tick;
    }

    public RenderAnimationInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }

}