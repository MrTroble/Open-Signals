package eu.gir.girsignals.guis;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import eu.gir.girsignals.guis.GuiElements.GuiEnumerableSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiBase extends GuiScreen {

	public static final int TOP_STRING_OFFSET = 15;
	public static final float STRING_SCALE = 1.5f;
	public static final int STRING_COLOR = 4210752;
	public static final int LEFT_OFFSET = 20;
	public static final int SIGNALTYPE_FIXED_WIDTH = 150;
	public static final int SIGNALTYPE_INSET = 20;
	public static final int MAXIMUM_GUI_HEIGHT = 320;
	public static final int GUI_INSET = 40;
	public static final int SIGNAL_RENDER_WIDTH_AND_INSET = 180;
	public static final int TOP_OFFSET = GUI_INSET;
	public static final int SIGNAL_TYPE_ID = -100;
	public static final int ELEMENT_SPACING = 10;
	public static final int BOTTOM_OFFSET = 30;
	public static final int DEFAULT_ID = 200;
	public static final int PAGE_SELECTION_ID = -890;
	public static final int TEXT_FIELD_ID = -200;

	private static final ResourceLocation CREATIVE_TAB = new ResourceLocation(
			"textures/gui/container/creative_inventory/tab_inventory.png");

	protected int guiLeft;
	protected int guiTop;
	protected int xSize = 340;
	protected int ySize = 230;
	private ArrayList<ArrayList<GuiEnumerableSetting>> pageList = new ArrayList<>();
	protected GuiElements.GuiPageSelect pageselect = new GuiElements.GuiPageSelect(pageList);

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		this.mc = mc;
		this.itemRender = mc.getRenderItem();
		this.fontRenderer = mc.fontRenderer;
		this.width = width;
		this.height = height;
		if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS
				.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
			this.initGui();
		}
		net.minecraftforge.common.MinecraftForge.EVENT_BUS
				.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		mc.getTextureManager().bindTexture(CREATIVE_TAB);

		DrawUtil.drawBack(this, guiLeft, guiLeft + xSize, guiTop, guiTop + ySize);

		synchronized (buttonList) {
			super.drawScreen(mouseX, mouseY, partialTicks);

			for (GuiButton guiButton : buttonList) {
				if (guiButton instanceof GuiEnumerableSetting) {
					if (((GuiEnumerableSetting) guiButton).drawHoverText(mouseX, mouseY, fontRenderer, this.xSize,
							this.ySize)) {
						break;
					}
				}
			}
		}
	}

	public void initButtons() {
		synchronized (buttonList) {
			this.buttonList.clear();
			this.buttonList.add(pageselect);
		}
	}

	@Override
	public void initGui() {
		if (buttonList.isEmpty())
			this.initButtons();
		int maxWidth = 0;
		for (GuiButton guiButton : buttonList) {
			if (guiButton instanceof GuiEnumerableSetting) {
				final GuiEnumerableSetting setting = (GuiEnumerableSetting) guiButton;
				final int width = setting.getMaxWidth(fontRenderer);
				if (maxWidth < width) {
					maxWidth = width;
				}
			}
		}
		maxWidth = Math.max(SIGNALTYPE_FIXED_WIDTH, maxWidth) + 20;
		this.ySize = Math.min(MAXIMUM_GUI_HEIGHT, this.height - GUI_INSET);
		this.xSize = maxWidth + SIGNAL_RENDER_WIDTH_AND_INSET + SIGNALTYPE_INSET;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		int index = 0;
		boolean visible = pageselect.getValue() == index;
		pageList.clear();
		pageList.add(Lists.newArrayList());
		final int xPos = this.guiLeft + LEFT_OFFSET;
		int yPos = this.guiTop + TOP_OFFSET;
		for (GuiButton guiButton : buttonList) {
			if (guiButton instanceof GuiEnumerableSetting) {
				final GuiEnumerableSetting setting = (GuiEnumerableSetting) guiButton;
				setting.setWidth(maxWidth);
				if (setting.equals(this.pageselect))
					continue;
				if ((yPos + ELEMENT_SPACING + setting.height) >= (this.guiTop + this.ySize - BOTTOM_OFFSET)) {
					pageList.add(Lists.newArrayList());
					yPos = this.guiTop + TOP_OFFSET;
					index++;
					visible = pageselect.getValue() == index;
				}
				pageList.get(index).add(setting);
				setting.updatePos(xPos, yPos);
				setting.update();
				setting.setVisible(visible);
				yPos += ELEMENT_SPACING + setting.height;
			}
		}

		this.pageselect.updatePos(this.guiLeft + maxWidth / 2, this.guiTop + this.ySize);
		this.pageselect.update();
		if (this.pageList.size() > 1) {
			this.pageselect.visible = true;
		} else {
			this.pageselect.visible = false;
		}
	}
}
