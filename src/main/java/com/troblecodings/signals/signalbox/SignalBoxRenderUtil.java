package com.troblecodings.signals.signalbox;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.troblecodings.guilib.ecs.entitys.UIEntity;

public final class SignalBoxRenderUtil {

    private SignalBoxRenderUtil() {}
    
    public static void drawRect(final int left, final int top, final int right, final int bottom,
            final int color) {
        // TODO COLORS
        final float f3 = (color >> 24 & 255) / 255.0F;
        final float f = (color >> 16 & 255) / 255.0F;
        final float f1 = (color >> 8 & 255) / 255.0F;
        final float f2 = (color & 255) / 255.0F;
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(left, bottom, 0.0D).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0D).endVertex();
        bufferbuilder.vertex(right, top, 0.0D).endVertex();
        bufferbuilder.vertex(left, top, 0.0D).endVertex();
        tessellator.end();
    }

    public static void drawTextured(final UIEntity entity, final int textureID) {
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        final float offset = 0.25f * textureID;
        bufferbuilder.vertex(0, entity.getHeight(), textureID).uv(offset, 0.5f).endVertex();
        bufferbuilder.vertex(entity.getWidth(), entity.getHeight(), textureID)
                .uv(offset + 0.25f, 0.5f).endVertex();
        bufferbuilder.vertex(entity.getWidth(), 0, textureID).uv(offset + 0.25f, 0).endVertex();
        bufferbuilder.vertex(0, 0, textureID).uv(offset, 0).endVertex();
        tessellator.end();
    }

    public static void drawLines(final int x1, final int x2, final int y1, final int y2,
            final int color) {
        // TODO COLORS
        final float f3 = (color >> 24 & 255) / 255.0F;
        final float f = (color >> 16 & 255) / 255.0F;
        final float f1 = (color >> 8 & 255) / 255.0F;
        final float f2 = (color & 255) / 255.0F;
        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuilder();
        GL11.glLineWidth(5);
        bufferbuilder.begin(Mode.LINE_STRIP, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(x1, y1, 0.0D).endVertex();
        bufferbuilder.vertex(x2, y2, 0.0D).endVertex();
        tessellator.end();

    }

    public static void drawPlatform(final Object parent, final int color) {
        SignalBoxRenderUtil.drawRect(0, 0, (int) ((UIEntity) parent).getWidth(),
                (int) ((UIEntity) parent).getHeight() / 3, color);
    }

    public static void drawBUE(final Object parent, final int color) {
        final int part = (int) (((UIEntity) parent).getHeight() / 3);
        SignalBoxRenderUtil.drawLines(0, (int) ((UIEntity) parent).getWidth(), part, part, color);
        SignalBoxRenderUtil.drawLines(0, (int) ((UIEntity) parent).getWidth(), part * 2, part * 2,
                color);
    }
}