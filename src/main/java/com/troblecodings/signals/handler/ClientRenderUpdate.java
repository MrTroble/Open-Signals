package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientRenderUpdate {

    public static final ClientRenderUpdate INSTANCE = new ClientRenderUpdate();
    private final Map<BlockPos, Long> highlightedBlocks = new HashMap<>();

    public void addHighlight(final BlockPos pos) {
        highlightedBlocks.put(pos, System.currentTimeMillis() + 10000);
    }

    public void clearHighlights() {
        highlightedBlocks.clear();
    }

    @SubscribeEvent
    public void render(final RenderWorldLastEvent event) {
        if (highlightedBlocks.isEmpty())
            return;
        final long current = System.currentTimeMillis();
        highlightedBlocks.entrySet().removeIf(entry -> entry.getValue() < current);

        final Entity player = Minecraft.getMinecraft().getRenderViewEntity();
        if (player == null)
            return;

        final double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        final double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        final double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();

        highlightedBlocks.forEach((pos, time) -> {
            RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(pos), 1, 0, 0, 1);
        });

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
