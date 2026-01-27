package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DisplaySpecialRenderer implements BlockEntityRenderer<DisplayTileEntity> {

    private final BlockEntityRendererProvider.Context context;

    public DisplaySpecialRenderer(final BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(final DisplayTileEntity tile, final float tick, final PoseStack stack,
            final MultiBufferSource source, final int rand1, final int rand2) {
        tile.renderOverlay(new RenderOverlayInfo(stack, 0, 0, 0, context.getFont()));
    }

}
