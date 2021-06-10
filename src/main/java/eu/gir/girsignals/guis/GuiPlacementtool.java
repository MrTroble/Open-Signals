package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.IIntegerable;
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

//		if (currentSelectedBlock.getCustomnameRenderHeight(null, null, null) != -1)
//			textField.drawTextBox();

		if (dragging) {
			animationState += mouseX - oldMouse;
			oldMouse = mouseX;
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) this.width * (5 / 6.0f), (float) this.height * (5 / 6.0f), 100 + this.zLevel);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.rotate(animationState, 0, 1, 0);
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

	private ArrayList<ArrayList<GuiButton>> pageList = new ArrayList<>();
	private int indexCurrentlyUsed = 0;

	@Override
	public void initGui() {
		textField = new GuiTextField(-200, fontRenderer, 10, 10, 100, 20);
		textField.setText(comp.getString(GIRNetworkHandler.SIGNAL_CUSTOMNAME));
		animationState = 180.0f;
		manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();

		this.buttonList.clear();

		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) currentSelectedBlock.getBlockState();
		Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		properties = unlistedProperties.toArray(new IUnlistedProperty[unlistedProperties.size()]);
		int maxWidth = 0;
		for (IUnlistedProperty<?> lenIUnlistedProperty : unlistedProperties) {
			maxWidth = Math.max(
					fontRenderer.getStringWidth(I18n.format("property." + lenIUnlistedProperty.getName() + ".name")),
					maxWidth);
		}
		maxWidth += 20;

		this.ySize = Math.min(290, this.height - 40);
		this.xSize = maxWidth * 2 + 80;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		int yPos = this.guiTop + 20;
		int xPos = this.guiLeft + 40;

		final GUIEnumerableSetting settings = new GUIEnumerableSetting(tool, -100, xPos, yPos, 150, "signaltype",
				this.implLastID, null);
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
		yPos += 30;
		for (IUnlistedProperty<?> property : unlistedProperties) {
			SEProperty<?> prop = SEProperty.cst(property);
			if (yPos >= (this.guiTop + this.ySize - 40)) {
				pageList.add(Lists.newArrayList());
				index++;
				yPos = this.guiTop + 50;
				visible = false;
			}
			int id = (yPos / 20) * (xPos / 50);
			String propName = property.getName();
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				final InternalCheckBox checkbox = new InternalCheckBox(id, xPos, yPos, propName,
						comp.getBoolean(propName));
				addButton(checkbox).visible = visible;
				pageList.get(index).add(checkbox);
				yPos += 10;
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				final GUIEnumerableSetting setting = new GUIEnumerableSetting(prop, id, xPos, yPos, maxWidth - 20,
						propName, comp.getInteger(propName), inp -> applyModelChanges());
				addButton(setting).visible = visible;
				pageList.get(index).add(setting);
				yPos += 20;
			}
			yPos += 10;
		}

		if (pageList.size() > 1) {
			final IIntegerable<String> sizeIn = SizeIntegerables.of(pageList.size(),
					idx -> (String) (idx + "/" + (pageList.size() - 1)));
			final GUIEnumerableSetting pageSelection = new GUIEnumerableSetting(sizeIn, -890, this.guiLeft + 40,
					this.guiTop + this.ySize - 30, maxWidth - 20, "page", indexCurrentlyUsed, inp -> {
						pageList.get(indexCurrentlyUsed).forEach(btn -> btn.visible = false);
						pageList.get(inp).forEach(btn -> btn.visible = true);
						indexCurrentlyUsed = inp;
					});
			addButton(pageSelection);
		}

		applyModelChanges();
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
			if (btn.id == -100 || btn.id == -890)
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
