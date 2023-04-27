package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class SignalSpecialRenderer extends TileEntityRenderer<SignalTileEntity> {

    private final TileEntityRendererDispatcher context;
    

    public SignalSpecialRenderer(final TileEntityRendererDispatcher context) {
        this.context = context;
    }

    @Override
    public void render(final SignalTileEntity tile, final float tick, final MatrixStack stack,
            final IRenderTypeBuffer source, final int rand1, final int rand2) {
        if (!tile.hasCustomName())
            return;
        tile.renderOverlay(new RenderOverlayInfo(stack, 0, 0, 0, context.getFont()));
    }
}