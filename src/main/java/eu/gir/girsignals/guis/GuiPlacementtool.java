package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.GuiSignalController.SizeIntegerables;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.items.Placementtool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiPlacementtool extends GuiScreen {

	private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation(
			"textures/gui/container/creative_inventory/tabs.png");
	private static final float DIM = 256.0f;

	private static final int TOP_STRING_OFFSET = 15;
	private static final float STRING_SCALE = 1.5f;
	private static final int STRING_COLOR = 4210752;
	private static final int LEFT_OFFSET = 20;
	private static final int SIGNALTYPE_FIXED_WIDTH = 150;
	private static final int SIGNALTYPE_INSET = 20;
	private static final int MAXIMUM_GUI_HEIGHT = 320;
	private static final int GUI_INSET = 40;
	private static final int SIGNAL_RENDER_WIDTH_AND_INSET = 180;
	private static final int TOP_OFFSET = GUI_INSET;
	private static final int SIGNAL_TYPE_ID = -100;
	private static final int SETTINGS_HEIGHT = 20;
	private static final int ELEMENT_SPACING = 10;
	private static final int BOTTOM_OFFSET = TOP_OFFSET;
	private static final int CHECK_BOX_HEIGHT = 10;
	private static final int DEFAULT_ID = 200;
	private static final int PAGE_SELECTION_ID = -890;
	private static final int TEXT_FIELD_ID = -200;

	@SuppressWarnings({ "rawtypes" })
	private IUnlistedProperty[] properties;
	private IExtendedBlockState ebs;
	private NBTTagCompound comp;
	private int usedBlock = 0;
	private BlockModelShapes manager;
	private Signal currentSelectedBlock;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	private Placementtool tool;
	private int implLastID = 0;
	private int guiLeft;
	private int guiTop;
	private int xSize = 340;
	private int ySize = 230;
	private float animationState = 0;
	private int oldMouse = 0;
	private boolean dragging = false;
	private GuiTextField textField;

	public GuiPlacementtool(ItemStack stack) {
		this.comp = stack.getTagCompound();
		if (comp == null)
			this.comp = new NBTTagCompound();
		tool = (Placementtool) stack.getItem();
		usedBlock = this.comp.hasKey(GIRNetworkHandler.BLOCK_TYPE_ID)
				? this.comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID)
				: tool.getObjFromID(0).getID();
		implLastID = tool.signalids.indexOf(usedBlock);
		currentSelectedBlock = Signal.SIGNALLIST.get(usedBlock);
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
	}

	private void drawBack(final int xLeft, final int xRight, final int yTop, final int yBottom) {
		mc.getTextureManager().bindTexture(CREATIVE_INVENTORY_TABS);

		drawTexturedModalRect(xLeft, yTop, 0, 32, 4, 4);
		drawTexturedModalRect(xLeft, yBottom, 0, 124, 4, 4);
		drawTexturedModalRect(xRight, yTop, 24, 32, 4, 4);
		drawTexturedModalRect(xRight, yBottom, 24, 124, 4, 4);

		drawScaledCustomSizeModalRect(xLeft + 4, yBottom, 4, 124, 1, 4, xRight - 4 - xLeft, 4, DIM, DIM);
		drawScaledCustomSizeModalRect(xLeft + 4, yTop, 4, 32, 1, 4, xRight - 4 - xLeft, 4, DIM, DIM);
		drawScaledCustomSizeModalRect(xLeft, yTop + 4, 0, 36, 4, 1, 4, yBottom - 4 - yTop, DIM, DIM);
		drawScaledCustomSizeModalRect(xRight, yTop + 4, 24, 36, 4, 1, 4, yBottom - 4 - yTop, DIM, DIM);

		drawRect(xLeft + 4, yTop + 4, xRight, yBottom, 0xFFC6C6C6);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		drawBack(guiLeft, guiLeft + xSize, guiTop, guiTop + ySize);

		super.drawScreen(mouseX, mouseY, partialTicks);

		if (currentSelectedBlock.getCustomnameRenderHeight(null, null, null) != -1)
			textField.drawTextBox();

		if (dragging) {
			animationState += mouseX - oldMouse;
			oldMouse = mouseX;
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + this.xSize - 70, this.guiTop + this.ySize / 2, 100.0f);
		GlStateManager.rotate(animationState, 0, 1, 0);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.translate(-0.5f, -3.5f, -0.5f);
		DrawUtil.draw(model.get());
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();

		for (GuiButton guiButton : buttonList) {
			if (guiButton instanceof InternalUnlocalized) {
				if (guiButton.isMouseOver()) {
					dragging = false;
					String str = I18n
							.format("property." + ((InternalUnlocalized) guiButton).getUnlocalized() + ".desc");
					this.drawHoveringText(Arrays.asList(str.split(System.lineSeparator())), mouseX, mouseY);
					break;
				}
			}

		}

		String s = I18n.format("property.signal.name");
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + LEFT_OFFSET, this.guiTop + TOP_STRING_OFFSET, 0);
		GlStateManager.scale(STRING_SCALE, STRING_SCALE, STRING_SCALE);
		this.fontRenderer.drawString(s, 0, 0, STRING_COLOR);
		GlStateManager.popMatrix();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public interface InternalUnlocalized {

		String getUnlocalized();

	}

	private static class InternalCheckBox extends GuiCheckBox implements InternalUnlocalized {

		public String unlocalized;

		public InternalCheckBox(int id, int xPos, int yPos, String displayString, boolean isChecked) {
			super(id, xPos, yPos, I18n.format("property." + displayString + ".name"), isChecked);
			this.unlocalized = displayString;
		}

		@Override
		public String getUnlocalized() {
			return unlocalized;
		}

	}

	private ArrayList<ArrayList<Object>> pageList = new ArrayList<>();
	private int indexCurrentlyUsed = 0;

	@Override
	public void initGui() {
		textField = new GuiTextField(TEXT_FIELD_ID, fontRenderer, 10, 10,
				SIGNALTYPE_FIXED_WIDTH + GUIEnumerableSetting.BUTTON_SIZE * 2 + GUIEnumerableSetting.OFFSET * 2,
				SETTINGS_HEIGHT);
		textField.setText(comp.getString(GIRNetworkHandler.SIGNAL_CUSTOMNAME));
		animationState = 180.0f;
		manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();

		this.buttonList.clear();

		final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) currentSelectedBlock.getBlockState();
		final Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		properties = unlistedProperties.toArray(new IUnlistedProperty[unlistedProperties.size()]);
		int maxWidth = 0;
		for (IUnlistedProperty<?> lenIUnlistedProperty : unlistedProperties) {
			maxWidth = Math.max(
					fontRenderer.getStringWidth(I18n.format("property." + lenIUnlistedProperty.getName() + ".name")),
					maxWidth);
		}
		maxWidth = Math.max(SIGNALTYPE_FIXED_WIDTH, maxWidth);

		this.ySize = Math.min(MAXIMUM_GUI_HEIGHT, this.height - GUI_INSET);
		this.xSize = maxWidth + SIGNAL_RENDER_WIDTH_AND_INSET + SIGNALTYPE_INSET;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		int yPos = this.guiTop + TOP_OFFSET;
		final int xPos = this.guiLeft + LEFT_OFFSET;

		final GUIEnumerableSetting settings = new GUIEnumerableSetting(tool, SIGNAL_TYPE_ID, xPos, yPos,
				SIGNALTYPE_FIXED_WIDTH, "signaltype", this.implLastID, null);
		settings.consumer = input -> {
			settings.enabled = false;
			implLastID = input;
			currentSelectedBlock = tool.getObjFromID(input);
			usedBlock = currentSelectedBlock.getID();
			ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
			initGui();
		};
		addButton(settings);

		pageList.clear();
		pageList.add(Lists.newArrayList());
		boolean visible = true;
		int index = indexCurrentlyUsed = 0;
		yPos += SETTINGS_HEIGHT + ELEMENT_SPACING;
		for (IUnlistedProperty<?> property : unlistedProperties) {
			SEProperty<?> prop = SEProperty.cst(property);
			if (!prop.isChangabelAtStage(ChangeableStage.APISTAGE) && !prop.isChangabelAtStage(ChangeableStage.GUISTAGE)
					&& !prop.equals(Signal.CUSTOMNAME))
				continue;
			if (yPos >= (this.guiTop + this.ySize - BOTTOM_OFFSET)) {
				pageList.add(Lists.newArrayList());
				index++;
				yPos = this.guiTop + SETTINGS_HEIGHT + ELEMENT_SPACING + TOP_OFFSET;
				visible = false;
			}
			String propName = property.getName();
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				final InternalCheckBox checkbox = new InternalCheckBox(DEFAULT_ID, xPos, yPos, propName,
						comp.getBoolean(propName));
				addButton(checkbox).visible = visible;
				pageList.get(index).add(checkbox);
				yPos += CHECK_BOX_HEIGHT;
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				final GUIEnumerableSetting setting = new GUIEnumerableSetting(prop, DEFAULT_ID, xPos, yPos, maxWidth,
						propName, comp.getInteger(propName), inp -> applyModelChanges());
				addButton(setting).visible = visible;
				pageList.get(index).add(setting);
				yPos += SETTINGS_HEIGHT;
			} else {
				textField.x = xPos;
				textField.y = yPos;
				yPos += SETTINGS_HEIGHT;
				textField.setVisible(visible);
				pageList.get(index).add(textField);
			}
			yPos += ELEMENT_SPACING;
		}

		if (pageList.size() > 1) {
			final IIntegerable<String> sizeIn = SizeIntegerables.of(pageList.size(),
					idx -> (String) (idx + "/" + (pageList.size() - 1)));
			final GUIEnumerableSetting pageSelection = new GUIEnumerableSetting(sizeIn, PAGE_SELECTION_ID, xPos,
					this.guiTop + this.ySize - BOTTOM_OFFSET + ELEMENT_SPACING, maxWidth, "page", indexCurrentlyUsed,
					inp -> {
						pageList.get(indexCurrentlyUsed).forEach(visible(false));
						pageList.get(inp).forEach(visible(true));
						indexCurrentlyUsed = inp;
					});
			addButton(pageSelection);
		}
		applyModelChanges();
	}

	private static Consumer<Object> visible(final boolean b) {
		return obj -> {
			if (obj instanceof GuiButton)
				((GuiButton) obj).visible = b;
			if (obj instanceof GuiTextField)
				((GuiTextField) obj).setVisible(b);
		};
	}

	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(GIRNetworkHandler.PLACEMENT_GUI_SET_NBT);
		buffer.writeInt(usedBlock);
		byte[] str = textField.getText().getBytes();
		buffer.writeInt(str.length);
		buffer.writeBytes(str);
		for (GuiButton button : buttonList) {
			if (button.id == -100)
				continue;
			if (button instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
				buffer.writeBoolean(buttonCheckBox.isChecked());
			} else if (button instanceof GUIEnumerableSetting) {
				GUIEnumerableSetting buttonCheckBox = (GUIEnumerableSetting) button;
				buffer.writeInt(buttonCheckBox.getValue());
			}
		}
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
		model.get().reset();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void applyModelChanges() {
		int i = 0;
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
		for (GuiButton btn : this.buttonList) {
			if (btn.id != DEFAULT_ID)
				continue;
			if (btn instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) btn;
				if (buttonCheckBox.isChecked()) {
					SEProperty property = (SEProperty) properties[i];
					ebs = ebs.withProperty(property, property.getDefault());
				}
			} else if (btn instanceof GUIEnumerableSetting) {
				GUIEnumerableSetting slider = (GUIEnumerableSetting) btn;
				SEProperty property = (SEProperty) properties[i];
				ebs = ebs.withProperty(property, property.getObjFromID(slider.value));
			}
			i++;
		}

		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(model.get(), manager, ebs);
		model.get().finishDrawing();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (currentSelectedBlock.getCustomnameRenderHeight(null, null, null) != -1)
			textField.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0) {
			this.dragging = true;
			oldMouse = mouseX;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		dragging = false;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		applyModelChanges();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (currentSelectedBlock.getCustomnameRenderHeight(null, null, null) != -1)
			textField.textboxKeyTyped(typedChar, keyCode);
	}

}
