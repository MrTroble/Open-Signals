package eu.gir.girsignals.guis;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.guis.guilib.entitys.UIComponent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class UISignalBoxTile extends UIComponent {
	
	private HashMap<EnumMode, Integer> signalBox = new HashMap<>();
	
	
	public UISignalBoxTile() {}

	
	public UISignalBoxTile(EnumMode... enumModes) {
		for(EnumMode mode : enumModes)
			signalBox.put(mode, 0xFF000000);
	}
	
	@Override
	public synchronized void draw(int mouseX, int mouseY) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		signalBox.forEach((mode, color) -> {
			drawLines((int)(mode.x1 * parent.getWidth()), (int)(mode.x2 * parent.getWidth()), (int)(mode.y1 * parent.getHeight()), (int)(mode.y2 * parent.getHeight()), color);
		});
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static enum EnumMode {
		
		STRAIGHT(0, 0.5, 1, 0.5),
		CORNER_LEFT_BOTTOM(0, 0.5, 0.5, 1),
		CORNER_LEFT_TOP(0, 0.5, 0.5, 0),
		CORNER_RIGHT_BOTTOM(0.5, 1, 1, 0.5),
		CORNER_RIGHT_TOP(0.5, 0, 1, 0.5);
		/**
		 * Bahnsteige oben und unten
		 * Signal End, HP, VP, RS, BUE, RA10, Naming
		 */
		
		public final double x1, x2, y1, y2;
		
		private EnumMode(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}
		
	}
	
	public synchronized void put(EnumMode mode, int color) {
		signalBox.put(mode, color);
	}
	
	public synchronized void toggle(EnumMode mode) {
		if(signalBox.containsKey(mode)) {
			signalBox.remove(mode);
		} else {
			signalBox.put(mode, 0xFF000000);
		}
		
	}
		
	@Override
	public void update() {
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
	
}
