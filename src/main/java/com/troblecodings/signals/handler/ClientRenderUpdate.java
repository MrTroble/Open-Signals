package com.troblecodings.signals.handler;

import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.signals.config.ConfigHandler;

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

    private BlockPos pos;
    private long time;

    public void addHighlight(final BlockPos pos) {
        this.pos = pos;
        this.time = System.currentTimeMillis() + ConfigHandler.highlightDuration * 1000;
    }

    public void clearHighlights() {
        this.pos = null;
        this.time = 0;
    }

    @SubscribeEvent
    public void render(final RenderWorldLastEvent event) {
        if (this.pos == null)
            return;
        final long current = System.currentTimeMillis();
        if (current > this.time) {
            this.pos = null;
            this.time = 0;
            return;
        }

        final Entity player = Minecraft.getMinecraft().getRenderViewEntity();
        if (player == null)
            return;

        final float partialTicks = event.getPartialTicks();
        final double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        final double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        final double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        DrawInfo drawInfo = new DrawInfo(0, 0, partialTicks);

        drawInfo.push();
        drawInfo.translate(-doubleX, -doubleY, -doubleZ);
        drawInfo.disableTexture();
        drawInfo.depthOff();

        RenderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(this.pos), 1, 0, 0, 1);

        drawInfo.depthOn();
        drawInfo.enableTexture(); // TODO: add a drawInfo method for this!
        drawInfo.pop();
    }
}
