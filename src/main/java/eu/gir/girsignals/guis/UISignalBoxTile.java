package eu.gir.girsignals.guis;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.signalbox.EnumGuiMode;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.guilib.ecs.entitys.UIComponent;
import eu.gir.guilib.ecs.interfaces.UIAutoSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {

    public static final ResourceLocation ICON = new ResourceLocation(GirsignalsMain.MODID,
            "gui/textures/symbols.png");

    private SignalBoxNode node;

    public UISignalBoxTile(final SignalBoxNode node) {
        this.node = node;
    }

    public UISignalBoxTile(final EnumGuiMode enumMode) {
        this.node = new SignalBoxNode(null);
        this.node.add(enumMode, Rotation.NONE);
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
        node.forEach((mode, opt) -> {
            GlStateManager.pushMatrix();
            final int offsetX = parent.getWidth() / 2;
            final int offsetY = parent.getHeight() / 2;
            GlStateManager.translate(offsetX, offsetY, 0);
            GlStateManager.rotate(mode.getValue().ordinal() * 90, 0, 0, 1);
            GlStateManager.translate(-offsetX, -offsetY, 0);
            mode.getKey().consumer.accept(parent, opt.getPathUsage().getColor());
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
    public void write(final NBTTagCompound compound) {
        this.node.write(compound);
    }

    @Override
    public void read(final NBTTagCompound compound) {
        this.node.read(compound);
    }

    @Override
    public String getID() {
        return this.node.getID();
    }

    @Override
    public void setID(final String id) {
        this.node.setID(id);
    }

}
