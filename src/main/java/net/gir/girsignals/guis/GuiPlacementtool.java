package net.gir.girsignals.guis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.gir.girsignals.EnumSignals.IIntegerable;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.SEProperty;
import net.gir.girsignals.SEProperty.ChangeableStage;
import net.gir.girsignals.blocks.SignalBlock;
import net.gir.girsignals.init.GIRNetworkHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
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

	public GuiPlacementtool(NBTTagCompound comp) {
		this.comp = comp;
		if (comp == null)
			this.comp = new NBTTagCompound();
		usedBlock = this.comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID);
		currentSelectedBlock = SignalBlock.SIGNALLIST.get(usedBlock);
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
	}

	private float animationState = 0;
	private int oldMouse = 0;
	private boolean dragging = false;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		for (GuiButton guiButton : buttonList) {
			if(guiButton instanceof InternalUnlocalized) {
				if(guiButton.isMouseOver()) {
					dragging = false;
					String str = I18n.format("property." + ((InternalUnlocalized) guiButton).getUnlocalized() + ".desc");
					if(str != null)
						this.drawHoveringText(Arrays.asList(str.split("%n")), mouseX, mouseY);
					break;
				}
			}
			
		}

		if (dragging) {
			animationState += mouseX - oldMouse;
			oldMouse = mouseX;
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.translate((float) this.width * (5 / 6.0f), (float) this.height * (5 / 6.0f),
				100.0F + this.zLevel);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.enableLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.rotate(animationState, 0, 1, 0);
		GlStateManager.enableBlend();
		GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		DrawUtil.draw(model.get());
		GlStateManager.disableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.disableDepth();
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
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
	
	@Override
	public void initGui() {
		animationState = 180.0f;
		manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();
		this.buttonList.clear();
		ebs = (IExtendedBlockState) SignalBlock.SIGNALLIST.get(usedBlock).getDefaultState();
		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) SignalBlock.SIGNALLIST.get(usedBlock)
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

		addButton(new GUISettingsSlider(new IIntegerable<String>() {

			@Override
			public String getObjFromID(int obj) {
				currentSelectedBlock = SignalBlock.SIGNALLIST.get(obj);
				ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
				return currentSelectedBlock.getLocalizedName();
			}

			@Override
			public int count() {
				return SignalBlock.SIGNALLIST.size();
			}

		}, -100, (this.width - 300) / 2, 10, 300, "signaltype", this.usedBlock, input -> {
			if (usedBlock != input) {
				usedBlock = input;
				initGui();
			}
		}));

		applyModelChanges();
	}

	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(usedBlock);
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
		
		DrawUtil.addToBuffer(model.get(), manager, ebs);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
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

}
