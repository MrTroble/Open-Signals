package net.gir.girsignals.guis;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.IIntegerable;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.SEProperty;
import net.gir.girsignals.SEProperty.ChangeableStage;
import net.gir.girsignals.blocks.SignalBlock;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.init.GIRNetworkHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.LightUtil.ItemConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
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
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(5000));
	private WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();
	
	public GuiPlacementtool(NBTTagCompound comp) {
		this.comp = comp;
		if (comp == null)
			this.comp = new NBTTagCompound();
		usedBlock = this.comp.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID);
		currentSelectedBlock = SignalBlock.SIGNALLIST.get(usedBlock);
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
	}

	private float animationState = 0;
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		animationState += partialTicks;
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate((float)this.width * (5/6.0f), (float)this.height * (2/3.0f), 100.0F + this.zLevel);
        GlStateManager.scale(16.0F, -16.0F, 16.0F);
        GlStateManager.enableLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.rotate(animationState, 0, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		/*Tessellator tes = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tes.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		bufferbuilder.addVertexData(model.get().getByteBuffer());
		tes.draw();*/
        uploader.draw(model.get());
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

	@Override
	public void initGui() {
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
			String propName = I18n.format("property." + property.getName() + ".name");
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				addButton(new GuiCheckBox(id, xPos, yPos, propName, comp.getBoolean(property.getName())));
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				addButton(new GUISettingsSlider(prop, id, xPos, yPos, maxWidth - 20, propName,
						comp.getInteger(property.getName())));
			}
		}

		addButton(new GUISettingsSlider(new IIntegerable<String>() {

			@Override
			public String getObjFromID(int obj) {
				if (usedBlock != obj) {
					usedBlock = obj;
					initGui();
				}
				currentSelectedBlock = SignalBlock.SIGNALLIST.get(obj);
				ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
				return currentSelectedBlock.getLocalizedName();
			}

			@Override
			public int count() {
				return SignalBlock.SIGNALLIST.size();
			}

		}, -100, (this.width - 300) / 2, 10, 300, I18n.format("property.signaltype.name"), this.usedBlock));

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
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void applyModelChanges() {
		int i = 0;
		ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();
		for (GuiButton btn : this.buttonList) {
			if (btn instanceof GuiCheckBox) {
				GuiCheckBox buttonCheckBox = (GuiCheckBox) btn;
				if (buttonCheckBox.isChecked()) {
					SEProperty property = (SEProperty) properties[i++];
					ebs = ebs.withProperty(property, property.getDefault());
				}
			}
		}
		IBakedModel mdl = manager.getModelForState(ebs);
		List<BakedQuad> lst = mdl.getQuads(ebs, null, 1);
		BufferBuilder builder = model.get();
		builder.reset();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		for (BakedQuad quad : lst) {
			LightUtil.renderQuadColor(builder, quad, 0xFFFFFFFF);
			System.out.println("TEST");
		}
		builder.finishDrawing();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		applyModelChanges();
	}

}
