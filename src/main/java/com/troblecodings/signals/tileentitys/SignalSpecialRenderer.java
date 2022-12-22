package com.troblecodings.signals.tileentitys;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class SignalSpecialRenderer implements BlockEntityRenderer<SignalTileEnity> {

    @SuppressWarnings("resource")
    @Override
    public void render(SignalTileEnity te, float tick, PoseStack stack, MultiBufferSource source,
            int rand1, int rand2) {
        if (!te.hasCustomName())
            return;
        te.renderOverlay(0, 0, 0, Minecraft.getInstance().font);
    }

}
