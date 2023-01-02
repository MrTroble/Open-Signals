package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SignalSpecialRenderer implements BlockEntityRenderer<SignalTileEntity> {

    private BlockEntityRendererProvider.Context context;

    public SignalSpecialRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(SignalTileEntity tile, float tick, PoseStack stack, MultiBufferSource source,
            int rand1, int rand2) {
        if (!tile.hasCustomName())
            return;
        tile.renderOverlay(new RenderOverlayInfo(stack, 0, 0, 0, context.getFont()));
    }

}
