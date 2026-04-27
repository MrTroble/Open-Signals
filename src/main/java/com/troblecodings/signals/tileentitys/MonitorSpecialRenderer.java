package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.core.RenderAnimationInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class MonitorSpecialRenderer implements BlockEntityRenderer<MonitorTileEntity> {

    private final BlockEntityRendererProvider.Context context;

    public MonitorSpecialRenderer(final BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(final MonitorTileEntity tile, final float tick, final PoseStack p_112309_,
            final MultiBufferSource source, final int rand1, final int rand2) {
        tile.render(new RenderAnimationInfo(p_112309_, context.getBlockRenderDispatcher(), source,
                rand1, rand2, tick));
    }

}
