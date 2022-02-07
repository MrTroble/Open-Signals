package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.util.Point;

import com.google.common.collect.Maps;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.guis.guilib.UIAutoSync;
import eu.gir.girsignals.guis.guilib.entitys.UIComponent;
import eu.gir.girsignals.signalbox.SignalBoxUtil.EnumGUIMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {
	
	private static ResourceLocation ICON = new ResourceLocation(GirsignalsMain.MODID, "gui/textures/symbols.png");
	
	private HashMap<Entry<EnumGUIMode, Rotation>, Integer> signalBox = new HashMap<>();
	private Point id = null;
	
	public UISignalBoxTile(Point name) {
		this.id = name;
	}
	
	public UISignalBoxTile(EnumGUIMode... enumModes) {
		for (EnumGUIMode mode : enumModes)
			signalBox.put(Maps.immutableEntry(mode, Rotation.NONE), 0xFF000000);
	}
	
	@Override
	public synchronized void draw(int mouseX, int mouseY) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(ICON);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.translate(0, 0, 1);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		signalBox.forEach((mode, opt) -> {
			GlStateManager.pushMatrix();
			final int offsetX = parent.getWidth() / 2;
			final int offsetY = parent.getHeight() / 2;
			GlStateManager.translate(offsetX, offsetY, 0);
			GlStateManager.rotate(mode.getValue().ordinal() * 90, 0, 0, 1);
			GlStateManager.translate(-offsetX, -offsetY, 0);
			mode.getKey().consumer.accept(parent, opt);
			GlStateManager.popMatrix();
		});
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public synchronized void put(EnumGUIMode mode, Rotation rotation, int color) {
		signalBox.put(Maps.immutableEntry(mode, rotation), color);
	}
	
	public synchronized void toggle(EnumGUIMode mode, Rotation rotation) {
		final Entry<EnumGUIMode, Rotation> entry = Maps.immutableEntry(mode, rotation);
		if (signalBox.containsKey(entry)) {
			signalBox.remove(entry);
		} else {
			signalBox.put(entry, 0xFF000000);
		}
		
	}
	
	@Override
	public void update() {
	}
	
	public void write(NBTTagCompound compound) {
		if (signalBox.isEmpty())
			return;
		final NBTTagCompound list = new NBTTagCompound();
		signalBox.forEach((key, val) -> list.setInteger(key.getKey().name() + "." + key.getValue().name(), val));
		compound.setTag(getID(), list);
	}
	
	@Override
	public void read(NBTTagCompound compound) {
		if (!compound.hasKey(getID()))
			return;
		signalBox.clear();
		final NBTTagCompound list = compound.getCompoundTag(getID());
		list.getKeySet().forEach(key -> {
			try {
				final String[] names = key.split("\\.");
				final EnumGUIMode mode = EnumGUIMode.valueOf(names[0]);
				final Rotation rotate = Rotation.valueOf(names[1]);
				put(mode, rotate, list.getInteger(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public String getID() {
		return this.id.getX() + "." + this.id.getY();
	}
	
	public Point getPoint() {
		return id;
	}
	
	@Override
	public void setID(String id) {
	}
	
}
