package com.troblecodings.signals.guis;

import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {

    public static final ResourceLocation ICON = new ResourceLocation(OpenSignalsMain.MODID,
            "gui/textures/symbols.png");

    private SignalBoxNode node;

    public UISignalBoxTile(final SignalBoxNode node) {
        this.node = node;
    }

    public UISignalBoxTile(final EnumGuiMode enumMode) {
        this.node = new SignalBoxNode((Point) null);
        this.node.add(new ModeSet(enumMode, Rotation.NONE));
    }

    @Override
    public synchronized void draw(final int mouseX, final int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.translate(0, 0, 1);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        node.forEach((modeSet) -> {
            final EnumPathUsage usage = node.getOption(modeSet).map(
                    entry -> entry.getEntry(PathEntryType.PATHUSAGE).orElse(EnumPathUsage.FREE))
                    .orElse(EnumPathUsage.FREE);
            GlStateManager.pushMatrix();
            final int offsetX = parent.getWidth() / 2;
            final int offsetY = parent.getHeight() / 2;
            GlStateManager.translate(offsetX, offsetY, 0);
            GlStateManager.rotate(modeSet.rotation.ordinal() * 90, 0, 0, 1);
            GlStateManager.translate(-offsetX, -offsetY, 0);
            modeSet.mode.consumer.accept(parent, usage.getColor());
            GlStateManager.popMatrix();
        });
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
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

    @Override
    public void write(final CompoundTag compound) {
        this.node.writeEntryNetwork(compound, true);
    }

    @Override
    public void read(final CompoundTag compound) {
        this.node.readEntryNetwork(compound);
    }

    @Override
    public String getID() {
        return this.node.getIdentifier();
    }

    @Override
    public void setID(final String id) {
    }

}
