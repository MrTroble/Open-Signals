package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;

public class SignalSpecialRenderer implements BlockEntityRenderer<SignalTileEntity> {

    private final BlockEntityRendererProvider.Context context;

    public SignalSpecialRenderer(final BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(final SignalTileEntity tile, final float tick, final PoseStack stack,
            final MultiBufferSource source, final int rand1, final int rand2) {
        if (tile.hasCustomName()) {
            tile.renderOverlay(new RenderOverlayInfo(stack, 0, 0, 0, context.getFont()));
        }
        if (tile.getSignal().hasAnimation(tile.getLevel(), tile.getBlockPos())) {
            renderAnimation(new RenderAnimationInfo(stack, context.getBlockRenderDispatcher(),
                    source, rand1, rand2), tile);
        } else if (!tile.getSignal().hasAnimation(tile.getLevel(), tile.getBlockPos())) {
            tile.animProgress = 0.0F;
        }
    }

    @SuppressWarnings("deprecation")
    public void renderAnimation(final RenderAnimationInfo info, final SignalTileEntity tile) {
        info.stack.pushPose(); // erst Berechnungen ausführen, dann Block rendern
        info.stack.translate(0.5f, 1, 0.5f); // Block verschieben

        info.stack.mulPose(Quaternion.fromXYZ(0, tile.animProgress * 0.005f, 0));
        tile.updateAnim(); // Progress aktualisieren

        info.stack.translate(-0.5f, 0, -0.5f); // Pivot Punkt verschieben

        info.dispatcher.renderSingleBlock(Blocks.GLASS.defaultBlockState(), info.stack, info.source,
                info.lightColor, info.overlayTexture); // Block rendern
        // Minecraft.getInstance().getModelManager().getBlockModelShaper()
        // .getBlockModel(tile.getBlockState());
        info.stack.popPose();
    }
}