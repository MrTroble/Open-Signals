package net.gir.girsignals.guis;

import java.io.IOException;
import java.util.Collection;

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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
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

	public GuiPlacementtool(NBTTagCompound comp) {
		this.comp = comp;
		if (comp == null)
			this.comp = new NBTTagCompound();
		usedBlock = comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.disableDepth();
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableLighting();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.translate((float) 100, (float) 100, 100.0F + this.zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, -1.0F, 1.0F);
		GlStateManager.scale(16.0F, 16.0F, 16.0F);

		IBakedModel model = this.mc.getBlockRendererDispatcher().getModelForState(ebs);
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tes.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);

		for (EnumFacing enumfacing : EnumFacing.values()) {
			for (BakedQuad quad : model.getQuads(ebs, enumfacing, 0L)) {
				LightUtil.renderQuadColor(bufferbuilder, quad, 0xFFFFFFFF);
			}
		}

		tes.draw();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		GlStateManager.enableDepth();
		RenderHelper.enableStandardItemLighting();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		ebs = (IExtendedBlockState) SignalBlock.SIGNALLIST.get(usedBlock).getDefaultState();
		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) SignalBlock.SIGNALLIST.get(usedBlock).getBlockState();
		Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		properties = unlistedProperties.toArray(new IUnlistedProperty[unlistedProperties.size()]);
		int maxWidth = 0;
		for (IUnlistedProperty<?> lenIUnlistedProperty : unlistedProperties)
			maxWidth = Math.max(fontRenderer.getStringWidth(I18n.format("property." + lenIUnlistedProperty.getName() + ".name")), maxWidth);
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
			String propName = I18n.format("property." + property.getName() + ".name");
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				addButton(new GuiCheckBox(id, xPos, yPos, propName, comp.getBoolean(property.getName())));
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				addButton(new GUISettingsSlider(prop, id, xPos, yPos, maxWidth - 20, propName, comp.getInteger(property.getName())));
			}
		}
		
		addButton(new GUISettingsSlider(new IIntegerable<String>() {

			@Override
			public String getObjFromID(int obj) {
				if(usedBlock != obj) {
					usedBlock = obj;
					initGui();
				}
				return SignalBlock.SIGNALLIST.get(obj).getLocalizedName();
			}

			@Override
			public int count() {
				return SignalBlock.SIGNALLIST.size();
			}
			
		}, -100, (this.width - 300) / 2, 10, 300, I18n.format("property.signaltype.name"), this.usedBlock));
	}

	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(usedBlock); // TODO Automatisieren für alle Signale
		for (GuiButton button : buttonList) {
			if(button.id == -100) continue;
			if (button instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
				buffer.writeBoolean(buttonCheckBox.isChecked());
			} else if(button instanceof GUISettingsSlider) {
				GUISettingsSlider buttonCheckBox = (GUISettingsSlider) button;
				buffer.writeInt(buttonCheckBox.getValue());
			}
		}
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button instanceof GuiCheckBox) {
			// TODO Block change
			ebs = (IExtendedBlockState) SignalBlock.SIGNALLIST.get(usedBlock).getDefaultState();
			int i = 0;
			for (GuiButton btn : this.buttonList) {
				if (btn instanceof GuiCheckBox) {
					GuiCheckBox buttonCheckBox = (GuiCheckBox) btn;
					if (buttonCheckBox.isChecked()) {
						SEProperty property = (SEProperty) properties[i++];
						ebs = ebs.withProperty(property, property.getDefault());
					}
				}
			}
		}
	}

}
