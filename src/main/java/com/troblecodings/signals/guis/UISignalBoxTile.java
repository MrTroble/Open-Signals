package com.troblecodings.signals.guis;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Quaternion;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.entitys.UIComponent;
import com.troblecodings.guilib.ecs.interfaces.UIAutoSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.util.Lazy;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {

    public static final ResourceLocation ICON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/symbols.png");

    private static final Lazy<AbstractTexture> ICON_TEXTURE = () -> Minecraft.getInstance()
            .getTextureManager().getTexture(ICON);

    private SignalBoxNode node;

    public UISignalBoxTile(final SignalBoxNode node) {
        this.node = node;
    }

    public UISignalBoxTile(final EnumGuiMode enumMode) {
        this.node = new SignalBoxNode((Point) null);
        this.node.add(new ModeSet(enumMode, Rotation.NONE));
    }

    @Override
    public synchronized void draw(final DrawInfo info) {
        ICON_TEXTURE.get().bind();
        RenderSystem.enableBlend();
        info.stack.translate(0, 0, 1);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        node.forEach((modeSet) -> {
            final EnumPathUsage usage = node.getOption(modeSet).map(
                    entry -> entry.getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE))
                    .orElse(EnumPathUsage.FREE);
            info.stack.pushPose();
            final int offsetX = (int) parent.getWidth() / 2;
            final int offsetY = (int) parent.getHeight() / 2;
            info.stack.translate(offsetX, offsetY, 0);
            info.stack.mulPose(Quaternion.fromXYZ(0, modeSet.rotation.ordinal() * 90, 0));
            info.stack.translate(-offsetX, -offsetY, 0);
            modeSet.mode.consumer.accept(parent, usage.getColor());
            info.stack.popPose();
        });
        RenderSystem.disableBlend();
    }

    @Override
    public void update() {
    }

    public SignalBoxNode getNode() {
        return node;
    }

    public void setNode(final SignalBoxNode node) {
        this.node = node;
    }

    // TODO Sync
    @Override
    public void write(final NBTWrapper compound) {
    }

    @Override
    public void read(final NBTWrapper compound) {
    }

    @Override
    public String getID() {
        return this.node.getIdentifier();
    }

    @Override
    public void setID(final String id) {
    }

}
