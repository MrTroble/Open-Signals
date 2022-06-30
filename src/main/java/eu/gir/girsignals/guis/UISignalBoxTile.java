package eu.gir.girsignals.guis;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.signalbox.ModeSet;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
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
    public void write(final NBTTagCompound compound) {
        this.node.writeEntryNetwork(compound, true);
    }

    @Override
    public void read(final NBTTagCompound compound) {
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
