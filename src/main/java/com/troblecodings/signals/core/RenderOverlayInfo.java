package com.troblecodings.signals.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

public class RenderOverlayInfo {

    public final PoseStack stack;
    public final double x;
    public final double y;
    public final double z;
    public SignalTileEntity tileEntity;
    public final Font font;
    /**
     * 1.20 removed {@code Font.draw(PoseStack, ...)}; in-world text goes through
     * {@code Font.drawInBatch}, which needs the renderer's buffer source.
     */
    public final MultiBufferSource source;

    public RenderOverlayInfo(final PoseStack stack, final double x, final double y, final double z,
            final Font font, final MultiBufferSource source) {
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
        this.font = font;
        this.source = source;
    }

    /**
     * Draws text the way 1.18's Font.draw did: no shadow, full brightness.
     */
    public void drawText(final String text, final float x, final float y, final int color) {
        font.drawInBatch(text, x, y, color, false, stack.last().pose(), source,
                Font.DisplayMode.NORMAL, 0, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
    }

    public RenderOverlayInfo with(final SignalTileEntity tileEntity) {
        this.tileEntity = tileEntity;
        return this;
    }
}