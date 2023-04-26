package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SignalSpecialRenderer implements BlockEntityRenderer<SignalTileEntity> {

    private final BlockEntityRendererProvider.Context context;

    public SignalSpecialRenderer(final BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(final SignalTileEntity tile, final float tick, final PoseStack stack,
            final MultiBufferSource source, final int rand1, final int rand2) {
        if (!tile.hasCustomName())
            return;
        tile.renderOverlay(new RenderOverlayInfo(stack, 0, 0, 0, context.getFont()));
    }
}