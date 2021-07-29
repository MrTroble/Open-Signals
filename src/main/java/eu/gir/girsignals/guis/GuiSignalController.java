package eu.gir.girsignals.guis;

import static eu.gir.girsignals.guis.GuiPlacementtool.BOTTOM_OFFSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.DEFAULT_ID;
import static eu.gir.girsignals.guis.GuiPlacementtool.ELEMENT_SPACING;
import static eu.gir.girsignals.guis.GuiPlacementtool.GUI_INSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.LEFT_OFFSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.MAXIMUM_GUI_HEIGHT;
import static eu.gir.girsignals.guis.GuiPlacementtool.PAGE_SELECTION_ID;
import static eu.gir.girsignals.guis.GuiPlacementtool.SETTINGS_HEIGHT;
import static eu.gir.girsignals.guis.GuiPlacementtool.SIGNALTYPE_FIXED_WIDTH;
import static eu.gir.girsignals.guis.GuiPlacementtool.SIGNALTYPE_INSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.SIGNAL_RENDER_WIDTH_AND_INSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.SIGNAL_TYPE_ID;
import static eu.gir.girsignals.guis.GuiPlacementtool.STRING_COLOR;
import static eu.gir.girsignals.guis.GuiPlacementtool.STRING_SCALE;
import static eu.gir.girsignals.guis.GuiPlacementtool.TOP_OFFSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.TOP_STRING_OFFSET;
import static eu.gir.girsignals.guis.GuiPlacementtool.visible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity.EnumRedstoneMode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiContainer {

	private ArrayList<ArrayList<Object>> pageList = new ArrayList<>();
	private final BlockPos pos;
	private BlockModelShapes manager;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	private final ArrayList<IUnlistedProperty<?>> properties = new ArrayList<>();

	public GuiSignalController(final SignalControllerTileEntity entity) {
		super(null);
		this.inventorySlots = new ContainerSignalController(entity, this);
		this.pos = entity.getPos();
	}

	public static enum EnumMode {
		MANUELL, REDSTONE
	}

	public static enum EnumMuxMode {
		MUX_CONTROL, SIGNAL_CONTROL
	}

	public class Setting {
		public final IIntegerable<?> iintg;
		public final String name;
		public final int defaultValue;
		public final Consumer<Integer> consumer;

		public Setting(final IIntegerable<?> iintg, final String name, final int defaultValue,
				final Consumer<Integer> consumer) {
			this.iintg = iintg;
			this.name = name;
			this.defaultValue = defaultValue;
			this.consumer = consumer;
		}
	}

	@Override
	public void initGui() {
		this.mc.player.openContainer = this.inventorySlots;
		this.manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();

		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);
		if (sigController.signalType < 0 || !sigController.hasLink)
			return;
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);

		properties.clear();
		int maxWidth = 0;
		for (final int id : sigController.supportedSigTypes) {
			final SEProperty<?> lenIUnlistedProperty = (SEProperty<?>) signal.getPropertyFromID(id);
			properties.add(lenIUnlistedProperty);
			int maxSelection = 0;
			for (int i = 0; i < lenIUnlistedProperty.count(); i++) {
				final String name = lenIUnlistedProperty.getObjFromID(i).toString();
				maxSelection = Math.max(maxSelection, fontRenderer.getStringWidth(name));
			}
			maxWidth = Math.max(
					fontRenderer.getStringWidth(I18n.format("property." + lenIUnlistedProperty.getName() + ".name"))
							+ maxSelection + 15,
					maxWidth);
		}
		maxWidth = Math.max(SIGNALTYPE_FIXED_WIDTH, maxWidth);
		this.xSize = maxWidth + SIGNAL_RENDER_WIDTH_AND_INSET + SIGNALTYPE_INSET;
		this.ySize = Math.min(MAXIMUM_GUI_HEIGHT, this.height - GUI_INSET);
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.buttonList.clear();

		int yPos = this.guiTop + TOP_OFFSET;
		final int xPos = this.guiLeft + LEFT_OFFSET;

		final EnumIntegerable<EnumMode> modeIntegerable = new EnumIntegerable<>(EnumMode.class);

		final GuiEnumerableSetting settings = new GuiEnumerableSetting(modeIntegerable, SIGNAL_TYPE_ID, xPos, yPos,
				maxWidth, "signaltype", sigController.indexMode.ordinal(), input -> {
					sigController.indexMode = EnumMode.values()[input];
					sigController.indexCurrentlyUsed = 0;
					initGui();
				});
		addButton(settings);
		int index = 0;
		pageList.clear();

		if (sigController.indexMode == EnumMode.MANUELL) {
			pageList.add(Lists.newArrayList());
			boolean visible = true;
			yPos += SETTINGS_HEIGHT + ELEMENT_SPACING;
			for (int i = 0; i < properties.size(); i++) {
				final int id = i;
				final SEProperty<?> prop = SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[i]));
				final int state = sigController.supportedSigStates[i];
				if (state < 0 || state >= prop.count())
					continue;
				if (!prop.isChangabelAtStage(ChangeableStage.APISTAGE))
					continue;
				if (yPos >= (this.guiTop + this.ySize - BOTTOM_OFFSET)) {
					pageList.add(Lists.newArrayList());
					index++;
					yPos = this.guiTop + SETTINGS_HEIGHT + ELEMENT_SPACING + TOP_OFFSET;
				}
				visible = index == sigController.indexCurrentlyUsed;
				String propName = prop.getName();
				final GuiEnumerableSetting setting = new GuiEnumerableSetting(prop, DEFAULT_ID, xPos, yPos, maxWidth,
						propName, state, inp -> sendChanges(id, inp));
				addButton(setting).visible = visible;
				pageList.get(index).add(setting);
				yPos += SETTINGS_HEIGHT;
				yPos += ELEMENT_SPACING;
			}
		} else if (sigController.indexMode == EnumMode.REDSTONE) {
			final int fLen = sigController.supportedSigTypes.length;
			final SizeIntegerables<String> possibleSignalTypesIntegerable = new SizeIntegerables<>(fLen + 1, in -> {
				if (in >= fLen)
					return "None";
				final int type = sigController.supportedSigTypes[in];
				final String name = signal.getPropertyFromID(type).getName();
				return I18n.format("property." + name + ".name");
			});
			final int config = sigController.facingRedstoneModes[sigController.faceUsed.ordinal()];
			final int[] unpacked = SignalControllerTileEntity.unpack(config);
			final int sigType = unpacked[0];
			final int sigOn = unpacked[1];
			final int sigOff = unpacked[2];

			final SEProperty<?> prop = sigType < sigController.supportedSigTypes.length
					? SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[sigType]))
					: null;

			final Setting rs_mode_setting = new Setting(new EnumIntegerable<>(EnumRedstoneMode.class), "rsmode",
					sigController.rsMode.ordinal(), in -> {
						sigController.rsMode = EnumRedstoneMode.values()[in];
						sendToPos(GIRNetworkHandler.SIG_CON_RS_SET, buffer -> {
							buffer.writeInt(in);
						});
					});

			final Setting[] settingList = sigController.rsMode == EnumRedstoneMode.SINGLE
					? new Setting[] { rs_mode_setting, new Setting(new EnumIntegerable<>(EnumFacing.class), "face",
							sigController.faceUsed.ordinal(), in -> {
								sigController.faceUsed = EnumFacing.values()[in];
								initGui();
							}),
							new Setting(possibleSignalTypesIntegerable, "sigtype", sigType > fLen ? fLen : sigType,
									in -> sendPacked(pack(in, sigOn, sigOff))),
							new Setting(prop, "sigon", sigOn, in -> sendPacked(pack(sigType, in, sigOff))),
							new Setting(prop, "sigoff", sigOff, in -> sendPacked(pack(sigType, sigOn, in))) }
					: new Setting[] { rs_mode_setting, new Setting(new EnumIntegerable<>(EnumMuxMode.class), "muxmode",
							sigController.muxMode.ordinal(), in -> {
								sigController.muxMode = EnumMuxMode.values()[in];
								for (int i = 0; i < sigController.facingRedstoneModes.length; i++) {
									if (sigController.facingRedstoneModes[i] == in) {
										sigController.faceUsed = EnumFacing.values()[i];
										break;
									}
								}
								initGui();
							}), new Setting(new EnumIntegerable<>(EnumFacing.class), "face",
									sigController.faceUsed.ordinal(), in -> {
										sigController.faceUsed = EnumFacing.values()[in];
										for (int i = 0; i < sigController.facingRedstoneModes.length; i++) {
											if (sigController.facingRedstoneModes[i] == sigController.muxMode
													.ordinal()) {
												final int idx = i;
												sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, bf -> {
													bf.writeInt(idx);
													bf.writeInt(3);
												});
											}
										}
										sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, bf -> {
											bf.writeInt(in);
											bf.writeInt(sigController.muxMode.ordinal());
										});
										initGui();
									}) };

			pageList.add(Lists.newArrayList());
			yPos = this.guiTop + SETTINGS_HEIGHT + ELEMENT_SPACING + TOP_OFFSET;
			for (final Setting set : settingList) {
				if (set.iintg == null)
					continue;
				if (yPos >= (this.guiTop + this.ySize - BOTTOM_OFFSET)) {
					pageList.add(Lists.newArrayList());
					index++;
					yPos = this.guiTop + SETTINGS_HEIGHT + ELEMENT_SPACING + TOP_OFFSET;
				}
				final GuiEnumerableSetting rsModeSetting = new GuiEnumerableSetting(set.iintg, SIGNAL_TYPE_ID + 1, xPos,
						yPos, maxWidth, set.name, set.defaultValue < set.iintg.count() ? set.defaultValue : 0,
						set.consumer);
				addButton(rsModeSetting);
				rsModeSetting.visible = index == sigController.indexCurrentlyUsed;
				pageList.get(index).add(rsModeSetting);
				yPos += SETTINGS_HEIGHT + ELEMENT_SPACING;
			}
		}
		if (pageList.size() > 1) {
			final IIntegerable<String> sizeIn = SizeIntegerables.of(pageList.size(),
					idx -> (String) (idx + "/" + (pageList.size() - 1)));
			final GuiEnumerableSetting pageSelection = new GuiEnumerableSetting(sizeIn, PAGE_SELECTION_ID, 0,
					this.guiTop + this.ySize - BOTTOM_OFFSET + ELEMENT_SPACING, 0, "page",
					sigController.indexCurrentlyUsed, inp -> {
						pageList.get(sigController.indexCurrentlyUsed).forEach(visible(false));
						pageList.get(inp).forEach(visible(true));
						sigController.indexCurrentlyUsed = inp;
					}, false);
			pageSelection.setWidth(
					mc.fontRenderer.getStringWidth(pageSelection.displayString) + GuiEnumerableSetting.OFFSET * 2);
			pageSelection.x = this.guiLeft + ((maxWidth - pageSelection.width) / 2) + GuiEnumerableSetting.BUTTON_SIZE;
			pageSelection.update();
			addButton(pageSelection);
		}

		updateDraw();
	}

	public void sendPacked(final int packed) {
		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);
		sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, buffer -> {
			buffer.writeInt(sigController.faceUsed.ordinal());
			buffer.writeInt(packed);
		});
	}

	public int pack(final int sigTypeID, final int onSig, final int offSig) {
		return (sigTypeID & 0b00000000000000000000000000001111) | ((onSig & 0b00000000000000000000000000111111) << 4)
				| ((offSig & 0b00000000000000000000000000111111) << 10);
	}

	public static class EnumIntegerable<T extends Enum<T>> implements IIntegerable<T> {

		private Class<T> t;

		public EnumIntegerable(Class<T> t) {
			this.t = t;
		}

		@Override
		public T getObjFromID(int obj) {
			return t.getEnumConstants()[obj];
		}

		@Override
		public int count() {
			return t.getEnumConstants().length;
		}
	}

	public interface ObjGetter<D> {
		D getObjFrom(int x);
	}

	public static class SizeIntegerables<T> implements IIntegerable<T> {

		private final int count;
		private final ObjGetter<T> getter;

		private SizeIntegerables(final int count, final ObjGetter<T> getter) {
			this.count = count;
			this.getter = getter;
		}

		@Override
		public T getObjFromID(int obj) {
			return this.getter.getObjFrom(obj);
		}

		@Override
		public int count() {
			return count;
		}

		public static <T> IIntegerable<T> of(final int count, final ObjGetter<T> get) {
			return new SizeIntegerables<T>(count, get);
		}

	}

	@Override
	public void onGuiClosed() {
		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);
		sendToPos(GIRNetworkHandler.SIG_CON_SAVE_UI_STATE, buf -> {
			buf.writeInt(sigController.indexMode.ordinal());
			buf.writeInt(sigController.faceUsed.ordinal());
			buf.writeInt(sigController.indexCurrentlyUsed);
			buf.writeInt(sigController.muxMode.ordinal());
		});
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();

		DrawUtil.drawBack(this, this.guiLeft, this.guiLeft + this.xSize, this.guiTop, this.guiTop + this.ySize);
		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);

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

		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
		final String s = I18n.format("tile." + signal.getRegistryName().getResourcePath() + ".name")
				+ (sigController.entity.hasCustomName() ? " - " + sigController.entity.getName() : "");
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + LEFT_OFFSET, this.guiTop + TOP_STRING_OFFSET, 0);
		GlStateManager.scale(STRING_SCALE, STRING_SCALE, STRING_SCALE);
		this.fontRenderer.drawString(s, 0, 0, STRING_COLOR);
		GlStateManager.popMatrix();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateDraw() {
		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
		IExtendedBlockState ebs = (IExtendedBlockState) signal.getDefaultState();

		for (int i = 0; i < properties.size(); i++) {
			SEProperty prop = SEProperty.cst(properties.get(i));
			int sigState = sigController.supportedSigStates[i];
			if (sigState < 0 || sigState >= prop.count())
				continue;
			ebs = ebs.withProperty(prop, prop.getObjFromID(sigState));
		}
		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(model.get(), manager, ebs);
		model.get().finishDrawing();
	}

	public void sendChanges(final int id, final int data) {
		final ContainerSignalController sigController = ((ContainerSignalController) this.inventorySlots);

		sendToPos(GIRNetworkHandler.SIG_CON_GUI_MANUELL_SET, buffer -> {
			buffer.writeInt(sigController.supportedSigTypes[id]);
			buffer.writeInt(data);
		});
		updateDraw();
	}

	private void sendToPos(final byte id, final Consumer<ByteBuf> consumer) {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(id);
		buffer.writeInt(pos.getX());
		buffer.writeInt(pos.getY());
		buffer.writeInt(pos.getZ());
		consumer.accept(buffer);
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}
}
