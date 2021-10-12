package eu.gir.girsignals.guis;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.guis.guilib.DrawUtil.EnumIntegerable;
import eu.gir.girsignals.guis.guilib.DrawUtil.SizeIntegerables;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements.GuiEnumerableSetting;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity.PlanElement;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity.TrackPlan;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;

public class GuiSignalBox extends GuiBase {

	private String name;
	private TrackPlan plan;
	private EnumTab tab = EnumTab.USE;

	private final SignalBoxTileEntity box;

	public GuiSignalBox(final SignalBoxTileEntity box) {
		this.name = I18n.format("tile.signalbox.name");
		this.plan = box.getPlan();
		this.box = box;
	}

	@Override
	public String getTitle() {
		return this.name;
	}

	private static final float XSIZE = 15;
	private static final float YSIZE = 15;
	private static final float XOFFSET = XSIZE + 2;
	private static final float YOFFSET = YSIZE + 2;

	private final float transformX(final int x) {
		return x * XOFFSET + this.guiLeft + 20;
	}

	private final float transformY(final int x) {
		return x * YOFFSET + this.guiTop + 20 + 50;
	}

	private static enum EnumTab {
		USE, SETTING, EDIT
	}

	@Override
	public void initButtons() {
		super.initButtons();

		this.addButton(new GuiEnumerableSetting(new EnumIntegerable<EnumTab>(EnumTab.class), tab.ordinal(), s -> {
			tab = EnumTab.values()[s];
			initButtons();
			initGui();
		}));

		if (tab == EnumTab.SETTING) {
			if (box.strings.size() > 0) {
				this.addButton(new GuiEnumerableSetting(
						new SizeIntegerables<String>("test", box.strings.size(), box.strings::get), 0, s -> {
							// sync load call
						}));
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (plan == null || plan.elements == null)
			return;

		final ScaledResolution res = new ScaledResolution(this.mc);
		final double scale = (double) res.getScaleFactor();
		GL11.glScissor((int) (this.guiLeft * scale), (int) (this.guiTop * scale), (int) (this.xSize * scale),
				(int) ((this.ySize - YOFFSET)* scale));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		if (tab == EnumTab.EDIT) {
			for (int y = 0; y < 13; y++) {
				final int yPos = (int) transformY(y);
				final int yPos2 = yPos + (int) YSIZE;
				if (yPos >= this.guiTop + this.ySize - XOFFSET)
					break;
				for (int x = 0; x < 25; x++) {
					final int xPos = (int) transformX(x);
					final int xPos2 = xPos + (int) XSIZE;
					final int color = (xPos < mouseX && yPos < mouseY && xPos2 > mouseX && yPos2 > mouseY) ? 0xFF0000FF:0xFFFFFFFF;
					drawRect(xPos, yPos, xPos2, yPos2, color);
				}
			}
		}

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

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

	}

}
