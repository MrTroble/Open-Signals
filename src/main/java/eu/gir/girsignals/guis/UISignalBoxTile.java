package eu.gir.girsignals.guis;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.signalbox.EnumGuiMode;
import eu.gir.girsignals.signalbox.SignalNode;
import eu.gir.guilib.ecs.entitys.UIComponent;
import eu.gir.guilib.ecs.interfaces.UIAutoSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {
	
	private static ResourceLocation ICON = new ResourceLocation(GirsignalsMain.MODID, "gui/textures/symbols.png");
	
	private SignalNode node;
	
	public UISignalBoxTile(SignalNode node) {
		this.node = node;
	}
	
	public UISignalBoxTile(EnumGuiMode enumMode) {
		this.node = new SignalNode(null);
		this.node.add(enumMode, Rotation.NONE);
	}
	
	@Override
	public synchronized void draw(int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.translate(0, 0, 1);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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
	
	public SignalNode getNode() {
		return node;
	}
	
	public void setNode(SignalNode node) {
		this.node = node;
	}
	
	@Override
	public void write(NBTTagCompound compound) {
		this.node.write(compound);
	}
	
	@Override
	public void read(NBTTagCompound compound) {
		this.node.read(compound);
	}
	
	@Override
	public String getID() {
		return this.node.getID();
	}
	
	@Override
	public void setID(String id) {
		this.node.setID(id);
	}
	
}
