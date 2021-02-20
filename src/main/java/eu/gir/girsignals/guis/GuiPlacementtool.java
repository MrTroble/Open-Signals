package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.SignalBlock;
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
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiPlacementtool extends GuiScreen {

	@SuppressWarnings({ "rawtypes" })
	private IUnlistedProperty[] properties;
	private IExtendedBlockState ebs;

	private NBTTagCompound comp;
	private int usedBlock = 0;

	private BlockModelShapes manager;
	private SignalBlock currentSelectedBlock;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	private Placementtool tool;
	
	public GuiPlacementtool(ItemStack stack) {
		this.comp = stack.getTagCompound();
		if (comp == null)
			this.comp = new NBTTagCompound();
		tool = (Placementtool) stack.getItem();
		int signalBlockID = this.comp.hasKey(GIRNetworkHandler.BLOCK_TYPE_ID) ? this.comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID):tool.getObjFromID(0).getID();
		usedBlock = tool.getTransform(signalBlockID);
		currentSelectedBlock = SignalBlock.SIGNALLIST.get(signalBlockID);
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
	}

	private float animationState = 0;
	private int oldMouse = 0;
	private boolean dragging = false;
	private GuiTextField textField;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		if(currentSelectedBlock.getCustomnameRenderHeight() != -1)
			textField.drawTextBox();

		if (dragging) {
			animationState += mouseX - oldMouse;
			oldMouse = mouseX;
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) this.width * (5 / 6.0f), (float) this.height * (5 / 6.0f),
				100 + this.zLevel);
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

	@Override
	public void initGui() {
		textField = new GuiTextField(-200, fontRenderer, 10, 10, 100, 20);
		textField.setText(comp.getString(GIRNetworkHandler.SIGNAL_CUSTOMNAME));
		animationState = 180.0f;
		manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();
		this.buttonList.clear();
		ebs = (IExtendedBlockState) tool.getObjFromID(usedBlock).getDefaultState();
		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) tool.getObjFromID(usedBlock)
				.getBlockState();
		Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		properties = unlistedProperties.toArray(new IUnlistedProperty[unlistedProperties.size()]);
		int maxWidth = 0;
		for (IUnlistedProperty<?> lenIUnlistedProperty : unlistedProperties)
			maxWidth = Math.max(
					fontRenderer.getStringWidth(I18n.format("property." + lenIUnlistedProperty.getName() + ".name")),
					maxWidth);
		maxWidth += 40;

		int yPos = 20;
		int xPos = 50;
		for (IUnlistedProperty<?> property : unlistedProperties) {
			SEProperty<?> prop = SEProperty.cst(property);
			yPos += 25;
			if (yPos >= 220) {
				xPos += maxWidth;
				yPos = 45;
			}
			int id = (yPos / 20) * (xPos / 50);
			String propName = property.getName();
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				addButton(new InternalCheckBox(id, xPos, yPos, propName, comp.getBoolean(propName)));
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				addButton(new GUISettingsSlider(prop, id, xPos, yPos, maxWidth - 20, propName,
						comp.getInteger(propName), inp -> applyModelChanges()));
			}
		}

		addButton(new GUISettingsSlider(tool, -100, (this.width - 150) / 2, 10, 150, "signaltype", this.usedBlock, input -> {
			if (usedBlock != input) {
				usedBlock = input;
				currentSelectedBlock = tool.getObjFromID(usedBlock);
				ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
				initGui();
			}
		}));

		applyModelChanges();
	}

	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(GIRNetworkHandler.PLACEMENT_GUI_SET_NBT);
		buffer.writeInt(tool.getObjFromID(usedBlock).getID());
		byte[] str = textField.getText().getBytes();
		buffer.writeInt(str.length);
		buffer.writeBytes(str);
		for (GuiButton button : buttonList) {
			if (button.id == -100)
				continue;
			if (button instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
				buffer.writeBoolean(buttonCheckBox.isChecked());
			} else if (button instanceof GUISettingsSlider) {
				GUISettingsSlider buttonCheckBox = (GUISettingsSlider) button;
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
			if (btn.id == -100)
				continue;
			if (btn instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) btn;
				if (buttonCheckBox.isChecked()) {
					SEProperty property = (SEProperty) properties[i];
					ebs = ebs.withProperty(property, property.getDefault());
				}
			} else if (btn instanceof GUISettingsSlider) {
				GUISettingsSlider slider = (GUISettingsSlider) btn;
				if (slider.value != 0) {
					SEProperty property = (SEProperty) properties[i];
					ebs = ebs.withProperty(property, property.getObjFromID(slider.value));
				}
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
		if(currentSelectedBlock.getCustomnameRenderHeight() != -1)
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
		if(currentSelectedBlock.getCustomnameRenderHeight() != -1)
			textField.textboxKeyTyped(typedChar, keyCode);
	}

}
