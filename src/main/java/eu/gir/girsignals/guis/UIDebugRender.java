package eu.gir.girsignals.guis;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

import eu.gir.girsignals.signalbox.SignalNode;
import eu.gir.guilib.ecs.entitys.UIComponent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class UIDebugRender extends UIComponent {
	
	private final SignalNode node;
	
	public UIDebugRender(SignalNode node) {
		this.node = node;
	}
	
	@Override
	public void draw(int mouseX, int mouseY) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.translate(0, 0, 1);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 20);
		GlStateManager.scale(parent.getWidth(), parent.getHeight(), 1);
		GlStateManager.translate(0.5, 0.5, 0);
		node.connections().stream().forEach(e -> {
			final Point cP = new Point(node.getPoint());
			cP.setLocation(node.getPoint().getX() * -1, node.getPoint().getY() * -1);
			final Point p2 = new Point(e.getKey());
			p2.translate(cP);
			final Point p1 = new Point(e.getValue());
			p1.translate(cP);
			drawLines(p2.getX(), p1.getX(), p2.getY(), p1.getY(), 0xFFFF0000);
		});
		GlStateManager.popMatrix();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
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
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
}
