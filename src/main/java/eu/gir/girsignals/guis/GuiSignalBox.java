package eu.gir.girsignals.guis;

import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity.PlanElement;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity.TrackPlan;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;

public class GuiSignalBox extends GuiBase {

	private String name;
	private TrackPlan plan;

	public GuiSignalBox(final SignalBoxTileEntity box) {
		this.name = I18n.format("tile.signalbox.name");
		this.plan = box.getPlan();
	}

	@Override
	public String getTitle() {
		return this.name;
	}

	private static final float XSIZE = 20;
	private static final float YSIZE = 20;
	private static final float XOFFSET = XSIZE + 4;
	private static final float YOFFSET = YSIZE + 4;

	private final float transformX(final int x) {
		return x * XOFFSET + this.guiLeft + XSIZE;
	}

	private final float transformY(final int x) {
		return x * YOFFSET + this.guiTop + YSIZE + 15;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (plan == null || plan.elements == null)
			return;
		plan.elements.forEach((name, element) -> {
			if (element.connectedElements == null)
				return;
			element.connectedElements.forEach(n -> {
				final PlanElement pel = plan.elements.get(n);
				final double pos1_x = transformX(element.xPos);
				final double pos1_y = transformY(element.yPos);
				final double pos2_x = transformX(pel.xPos);
				final double pos2_y = transformY(pel.yPos);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				GlStateManager.enableBlend();
				GlStateManager.disableTexture2D();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO);
				GlStateManager.color(1, 0, 0, 1);
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
				bufferbuilder.pos((double) pos1_x + 3, (double) pos1_y, 0.0D).endVertex();
				bufferbuilder.pos((double) pos1_x, (double) pos1_y, 0.0D).endVertex();
				bufferbuilder.pos((double) pos2_x, (double) pos2_y, 0.0D).endVertex();
				bufferbuilder.pos((double) pos2_x + 3, (double) pos2_y, 0.0D).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			});
		});

		plan.elements.forEach((name, element) -> {
			float x = transformX(element.xPos);
			float y = transformY(element.yPos);
			drawGradientRect((int) x, (int) y, (int) (x + XSIZE), (int) (y + YSIZE), 0xFF0000FF, 0xFFFFFFFF);
		});
	}

}
