package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.DrawUtil;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiElements.GuiEnumerableSetting;
import eu.gir.girsignals.guis.guilib.GuiElements.GuiSettingCheckBox;
import eu.gir.girsignals.guis.guilib.IIntegerable;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.items.Placementtool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiButton;
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
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiPlacementtool extends GuiBase {

	private NBTTagCompound comp;
	private BlockModelShapes manager;
	private Signal currentSelectedBlock;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	private Placementtool tool;
	private int implLastID = 0;
	private float animationState = 0;
	private int oldMouse = 0;
	private boolean dragging = false;
	private HashMap<SEProperty<?>, Object> map = new HashMap<>();
	private final GuiElements.GuiSettingTextbox textbox;

	public GuiPlacementtool(ItemStack stack) {
		this.comp = stack.getTagCompound();
		if (comp == null)
			this.comp = new NBTTagCompound();
		tool = (Placementtool) stack.getItem();
		final int usedBlock = this.comp.hasKey(GIRNetworkHandler.BLOCK_TYPE_ID)
				? this.comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID)
				: tool.getObjFromID(0).getID();
		implLastID = tool.signalids.indexOf(usedBlock);
		currentSelectedBlock = Signal.SIGNALLIST.get(usedBlock);
		textbox = new GuiElements.GuiSettingTextbox(comp.getString(GIRNetworkHandler.SIGNAL_CUSTOMNAME),
				i -> applyModelChanges());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

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
	}

	@Override
	public String getTitle() {
		return I18n.format("property.signal.name");
	}

	@Override
	public void initGui() {
		animationState = 180.0f;
		manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();
		if (buttonList.isEmpty())
			this.initButtons();
		applyModelChanges();
		super.initGui();
	}

	@Override
	public void initButtons() {
		super.initButtons();

		final GuiEnumerableSetting settings = new GuiEnumerableSetting(tool, this.implLastID, null);
		settings.consumer = input -> {
			settings.enabled = false;
			implLastID = input;
			currentSelectedBlock = tool.getObjFromID(input);
			initButtons();
		};
		addButton(settings);

		final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) currentSelectedBlock.getBlockState();
		final Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		for (IUnlistedProperty<?> property : unlistedProperties) {
			final SEProperty<?> prop = SEProperty.cst(property);
			final String propName = property.getName();
			final int value = comp.getInteger(propName);
			GuiHandler.of(prop, value, inp -> applyModelChanges(), ChangeableStage.GUISTAGE).ifPresent(this::addButton);
		}

		initGui();
	}

	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(GIRNetworkHandler.PLACEMENT_GUI_SET_NBT);
		buffer.writeInt(currentSelectedBlock.getID());
		byte[] str = textbox.getText().getBytes();
		buffer.writeInt(str.length);
		buffer.writeBytes(str);
		for (GuiButton button : buttonList) {
			if (button instanceof GuiEnumerableSetting) {
				if (!button.equals(super.pageselect)) {
					GuiEnumerableSetting buttonCheckBox = (GuiEnumerableSetting) button;
					IIntegerable<?> iint = buttonCheckBox.property;
					if (iint instanceof SEProperty) {
						buffer.writeInt(currentSelectedBlock.getIDFromProperty(SEProperty.cst(iint)));
						buffer.writeInt(buttonCheckBox.getValue());
					}
				}
			}
		}
		buffer.writeInt(0xFFFFFFFF);
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
		model.get().reset();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void applyModelChanges() {
		IExtendedBlockState ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
		for (GuiButton btn : this.buttonList) {
			if (btn instanceof GuiEnumerableSetting) {
				final IIntegerable<?> iint = ((GuiEnumerableSetting) btn).property;
				if (iint instanceof SEProperty) {
					final SEProperty prop = SEProperty.cst(iint);
					if (btn instanceof GuiSettingCheckBox) {
						GuiSettingCheckBox buttonCheckBox = (GuiSettingCheckBox) btn;
						if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
							ebs = ebs.withProperty(prop, buttonCheckBox.isChecked());
						} else if (buttonCheckBox.isChecked()) {
							ebs = ebs.withProperty(prop, prop.getDefault());
						}
					} else if (btn instanceof GuiEnumerableSetting) {
						GuiEnumerableSetting slider = (GuiEnumerableSetting) btn;
						ebs = ebs.withProperty(prop, prop.getObjFromID(slider.getValue()));
					}
				}
			}
		}
		for (Entry<IUnlistedProperty<?>, Optional<?>> prop : ebs.getUnlistedProperties().entrySet()) {
			final SEProperty property = SEProperty.cst(prop.getKey());
			if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
				ebs = ebs.withProperty(property, property.getDefault());
			}
		}

		map.clear();
		ebs.getUnlistedProperties().forEach((prop, opt) -> opt.ifPresent(val -> map.put(SEProperty.cst(prop), val)));

		synchronized (textbox) {
			if (currentSelectedBlock.canHaveCustomname(map)) {
				if (!buttonList.contains(textbox)) {
					addButton(textbox);
					initGui();
				}
				if (!textbox.getText().isEmpty())
					ebs = ebs.withProperty(Signal.CUSTOMNAME, true);
			} else if (buttonList.contains(textbox)) {
				buttonList.remove(textbox);
			}
		}
		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(model.get(), manager, ebs);
		model.get().finishDrawing();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		synchronized (textbox) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			if (mouseButton == 0) {
				this.dragging = true;
				oldMouse = mouseX;
			}
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		dragging = false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
	}

}
