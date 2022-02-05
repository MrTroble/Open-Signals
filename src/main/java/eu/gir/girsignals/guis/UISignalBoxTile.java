package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.guis.guilib.UIAutoSync;
import eu.gir.girsignals.guis.guilib.entitys.UIComponent;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;

public class UISignalBoxTile extends UIComponent implements UIAutoSync {
	
	private static ResourceLocation ICON = new ResourceLocation(GirsignalsMain.MODID, "gui/textures/symbols.png");
	
	private HashMap<Entry<EnumMode, Rotation>, Integer> signalBox = new HashMap<>();
	private String id = "unknown";
	
	public UISignalBoxTile(String name) {
		this.id = name;
	}
	
	public UISignalBoxTile(EnumMode... enumModes) {
		for (EnumMode mode : enumModes)
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
	
	public static enum EnumMode {
		
		STRAIGHT(0, 0.5, 1, 0.5),
		CORNER(0, 0.5, 0.5, 1),
		END(1, 0.30, 1, 0.70),
		PLATFORM((parent, color) -> drawRect(0, 0, parent.getWidth(), parent.getHeight() / 3, color)),
		BUE((parent, color) -> {
			final int part = parent.getHeight() / 3;
			drawLines(0, parent.getWidth(), part, part, color);
			drawLines(0, parent.getWidth(), part * 2, part * 2, color);
		}),
		HP(0),
		VP(1),
		RS(2),
		RA10(3);
		
		/**
		 * Naming
		 */
		
		public final BiConsumer<UIEntity, Integer> consumer;
		
		private EnumMode(int id) {
			this.consumer = (parent, color) -> drawTextured(parent, id);
		}
		
		private EnumMode(double x1, double y1, double x2, double y2) {
			this.consumer = (parent, color) -> drawLines((int) (x1 * parent.getWidth()), (int) (x2 * parent.getWidth()), (int) (y1 * parent.getHeight()), (int) (y2 * parent.getHeight()), color);
		}
		
		private EnumMode(final BiConsumer<UIEntity, Integer> consumer) {
			this.consumer = consumer;
		}
		
	}
	
	public synchronized void put(EnumMode mode, Rotation rotation, int color) {
		signalBox.put(Maps.immutableEntry(mode, rotation), color);
	}
	
	public synchronized void toggle(EnumMode mode, Rotation rotation) {
		final Entry<EnumMode, Rotation> entry = Maps.immutableEntry(mode, rotation);
		if (signalBox.containsKey(entry)) {
			signalBox.remove(entry);
		} else {
			signalBox.put(entry, 0xFF000000);
		}
		
	}
	
	@Override
	public void update() {
	}
	
	private static void drawRect(int left, int top, int right, int bottom, int color) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(f, f1, f2, f3);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) left, (double) bottom, 0.0D).endVertex();
		bufferbuilder.pos((double) right, (double) bottom, 0.0D).endVertex();
		bufferbuilder.pos((double) right, (double) top, 0.0D).endVertex();
		bufferbuilder.pos((double) left, (double) top, 0.0D).endVertex();
		tessellator.draw();
	}
	
	private static void drawTextured(UIEntity entity, int textureID) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		final double offset = 0.25 * textureID;
		bufferbuilder.pos((double) 0, (double) entity.getHeight(), textureID).tex(offset, 1).endVertex();
		bufferbuilder.pos((double) entity.getWidth(), (double) entity.getHeight(), textureID).tex(offset + 0.25, 1).endVertex();
		bufferbuilder.pos((double) entity.getWidth(), (double) 0, textureID).tex(offset + 0.25, 0).endVertex();
		bufferbuilder.pos((double) 0, (double) 0, textureID).tex(offset, 0).endVertex();
		tessellator.draw();		
		GlStateManager.disableTexture2D();
	}
	
	private static void drawLines(int x1, int x2, int y1, int y2, int color) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GL11.glLineWidth(5);
		GlStateManager.color(f, f1, f2, f3);
		bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(x1, y1, 0.0D).endVertex();
		bufferbuilder.pos(x2, y2, 0.0D).endVertex();
		tessellator.draw();
	}
	
	@Override
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
				final EnumMode mode = EnumMode.valueOf(names[0]);
				final Rotation rotate = Rotation.valueOf(names[1]);
				put(mode, rotate, list.getInteger(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public String getID() {
		return this.id;
	}
	
	@Override
	public void setID(String id) {
		this.id = id;
	}
	
}
