package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.DrawUtil;
import eu.gir.girsignals.guis.guilib.DrawUtil.EnumIntegerable;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.entitys.UIButton;
import eu.gir.girsignals.guis.guilib.entitys.UIClickable;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIVBox;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSignalController extends GuiBase {

	private final BlockPos pos;
	private BlockModelShapes manager;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	public ContainerSignalController sigController;
	private UIEntity list;

	public GuiSignalController(final SignalControllerTileEntity entity) {
		super("TestTitle");
		this.sigController = new ContainerSignalController(entity);
		this.pos = entity.getPos();
		compound = entity.getTag();
		init();
	}

	private void initMode(EnumMode mode, Signal signal) {
		switch (mode) {
		case MANUELL:
			addManuellMode(signal);
			break;
		case SINGLE:
			break;
		case MUX:
			break;
		default:
			break;
		}
	}

	private void init() {
		if (sigController.signalType < 0 || !sigController.hasLink) {
			buttonList.clear();
			return;
		}
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);

		list = new UIEntity();

		final EnumIntegerable<EnumMode> enumMode = new EnumIntegerable<EnumMode>(EnumMode.class);
		final UIEntity rsMode = GuiElements.createEnumElement(enumMode, in -> {
			list.clearChildren();
			initMode(enumMode.getObjFromID(in), signal);
			this.list.read(compound);
		});
		this.entity.add(rsMode);
		
		list.setInheritBounds(true);
		list.add(new UIVBox(5));
		this.entity.add(list);
		this.entity.add(new UIVBox(5));
		
		final UIEntity applyButton = new UIEntity();
		applyButton.add(new UIButton("Apply"));
		applyButton.setBounds(100, 20);
		applyButton.add(new UIClickable(e -> {
			this.entity.write(compound);
			GuiSyncNetwork.sendToPosServer(compound, pos);
		}));
		
		this.entity.add(applyButton);
		this.entity.read(compound);
	}

	private HashMap<SEProperty<?>, Object> createMap(Signal signal) {
		final HashMap<SEProperty<?>, Object> map = Maps.newHashMap();
//		for (Entry<Integer, Integer> entry : sigController.guiCacheList) {
//			final SEProperty<?> prop = SEProperty.cst(signal.getPropertyFromID(entry.getKey()));
//			map.put(prop, prop.getObjFromID(entry.getValue()));
//		}

		for (int i = 0; i < sigController.supportedSigTypes.length; i++) {
			final SEProperty<?> prop = SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[i]));
			int sigState = sigController.supportedSigStates[i];
			if (sigState < 0 || sigState >= prop.count())
				sigState = 0;
			map.put(prop, prop.getObjFromID(sigState));
		}
		return map;
	}

	private void addManuellMode(final Signal signal) {
		HashMap<SEProperty<?>, Object> map = createMap(signal);
		for (SEProperty<?> entry : map.keySet()) {
			if (entry.test(map) && (entry.isChangabelAtStage(ChangeableStage.APISTAGE)
					|| entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))) {
				final UIEntity guiEnum = GuiElements.createEnumElement(entry, e -> {});
				list.add(guiEnum);
			}
		}
	}

	@Override
	public void initGui() {
		this.mc.player.openContainer = this.sigController;
		this.manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();
		super.initGui();
	}

	@Override
	public void onGuiClosed() {
		entity.write(compound);
		GuiSyncNetwork.sendToPosServer(compound, pos);
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (!sigController.hasLink) {
			final String s = "No Signal connected!";
			final int width = mc.fontRenderer.getStringWidth(s);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + (this.xSize - width * 2) / 2,
					this.guiTop + (this.ySize - mc.fontRenderer.FONT_HEIGHT) / 2 - 20, 0);
			GlStateManager.scale(2, 2, 2);
			mc.fontRenderer.drawStringWithShadow(s, 0, 0, 0xFFFF0000);
			GlStateManager.popMatrix();
			return;
		}

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + this.xSize - 70, this.guiTop + this.ySize / 2, 100.0f);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.translate(-0.5f, -3.5f, -0.5f);
		DrawUtil.draw(model.get());
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
	}

	@Override
	public String getTitle() {
		if (sigController.signalType < 0 || !sigController.hasLink)
			return "";
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
		return I18n.format("tile." + signal.getRegistryName().getResourcePath() + ".name")
				+ (sigController.entity.hasCustomName() ? " - " + sigController.entity.getName() : "");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateDraw() {
//		if (sigController.supportedSigStates == null || !sigController.hasLink)
//			return;
////		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
////		IExtendedBlockState ebs = (IExtendedBlockState) signal.getDefaultState();
////
////		for (Entry<Integer, Integer> entry : sigController.guiCacheList) {
////			SEProperty prop = SEProperty.cst(signal.getPropertyFromID(entry.getKey()));
////			ebs = ebs.withProperty(prop, prop.getObjFromID(entry.getValue()));
////		}
////
////		for (int i = 0; i < sigController.supportedSigStates.length; i++) {
////			int sigState = sigController.supportedSigStates[i];
////			SEProperty prop = SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[i]));
////			if (sigState < 0 || sigState >= prop.count())
////				continue;
////			ebs = ebs.withProperty(prop, prop.getObjFromID(sigState));
////		}
//		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//		DrawUtil.addToBuffer(model.get(), manager, ebs);
//		model.get().finishDrawing();
	}
}
