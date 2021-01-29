package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.SignalBlock;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiScreen {

	private GUISettingsSlider slider;
	private final SignalBlock block;
	private final SignalControllerTileEntity tile;
	private final HashMap<SEProperty<?>, Integer> properties = new HashMap<>();
	private final BlockPos pos;
	private final World world;

	public GuiSignalController(BlockPos pos, World world) {
		this.pos = pos;
		this.world = world;
		this.tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		if (!this.tile.hasLinkImpl()) {
			this.block = null;
			return;
		}
		IBlockState state = world.getBlockState(this.tile.getLinkedPosition());
		this.block = (SignalBlock) state.getBlock();
	}

	public static enum Stages {
		MANUELL, REDSTONE, SCRIPTING
	}

	public static enum RedstoneMode {
		INCREMENTAL, MUX, SINGEL
	}

	private Stages currentStage = Stages.MANUELL;

	@Override
	public void initGui() {
		EnumIntegerable<Stages> enumToInt = new EnumIntegerable<Stages>(Stages.class);
		this.slider = new GUISettingsSlider(enumToInt, -100, (this.width - 150) / 2, 10, 150, "stagetype",
				currentStage.ordinal(), in -> {
					currentStage = enumToInt.getObjFromID(in);
					this.buttonList.clear();
					initGui();
				});

		if (block == null) {
			// TODO No link message
			System.out.println("NOBLOCK");
			return;
		}
		this.addButton(slider);
		switch (currentStage) {
		case MANUELL:
			initManuell();
			break;
		case REDSTONE:
			initRedstone();
			break;
		case SCRIPTING:
			initScripting();
			break;
		default:
			break;
		}

		String applyStr = I18n.format("btn.apply");
		int applyWidth = fontRenderer.getStringWidth(applyStr) + 30;
		GuiButton apply = new GuiButton(-200, (this.width - applyWidth) / 2, this.height - 30, applyWidth, 20,
				applyStr);
		addButton(apply);
	}

	private void initManuell() {
		int maxWidth = 0;

		for (int x : tile.getSupportedSignalTypesImpl()) {
			SEProperty<?> prop = (SEProperty<?>) block.getPropertyFromID(x);
			String format = I18n.format("property." + prop.getName() + ".name");
			int maxProp = 0;
			if (prop.getType().isEnum()) {
				for (int i = 0; i < prop.count(); i++) {
					maxProp = Math.max(fontRenderer.getStringWidth(String.format(prop.getObjFromID(i).toString())),
							maxProp);
				}
			}
			maxWidth = Math.max(fontRenderer.getStringWidth(format) + maxProp, maxWidth);
			properties.put(prop, tile.getSignalStateImpl(block.getIDFromProperty(prop)));
		}
		maxWidth += 40;

		int y = 30;
		int x = 30;
		for (Entry<SEProperty<?>, Integer> entry : properties.entrySet()) {
			SEProperty<?> prop = entry.getKey();
			this.addButton(new GUISettingsSlider(prop, 0, x, (y += 25), maxWidth, prop.getName(),
					entry.getValue().intValue(), in -> properties.put(prop, in)));
			if (y >= 220) {
				y = 55;
				x += maxWidth + 20;
			}
		}
	}

	private RedstoneMode rsmode = RedstoneMode.SINGEL;

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

	private BufferBuilder builder = new BufferBuilder(100);

	private void initRedstone() {
		EnumIntegerable<RedstoneMode> rsmodeen = new EnumIntegerable<RedstoneMode>(RedstoneMode.class);
		GUISettingsSlider redstonemode = new GUISettingsSlider(rsmodeen, -100, (this.width - 150) / 2, 40, 150,
				"stagetype", rsmode.ordinal(), in -> {
					rsmode = rsmodeen.getObjFromID(in);
					this.buttonList.clear();
					initGui();
				});
		addButton(redstonemode);
				
		BlockModelShapes shapes = mc.getBlockRendererDispatcher().getBlockModelShapes();
		
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(builder, shapes, world.getBlockState(pos));
		
		for(EnumFacing face : EnumFacing.VALUES) {
			BlockPos curPos = pos.offset(face);
			IBlockState state = world.getBlockState(curPos);
			if(state != null && !state.getBlock().isAir(state, world, curPos)) {
				Vec3i dirVec = face.getDirectionVec();
				builder.setTranslation(dirVec.getX(), dirVec.getY(), dirVec.getZ());
				DrawUtil.addToBuffer(builder, shapes, state, 0x1FFFFFFF);
			}
		}
		builder.finishDrawing();
	}

	private void initScripting() {
		// TODO Add scripting
	}

	private float rotateY = 0, rotateX = 0, lastX = 0, lastY = 0;
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if(currentStage == Stages.REDSTONE) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			GlStateManager.enableRescaleNormal();
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) this.width * 0.5f, (float) this.height * 0.5f,
					100);
			GlStateManager.scale(32.0F, -32.0F, 32.0F);
			GlStateManager.rotate(rotateY, 1, 0, 0);
			GlStateManager.rotate(rotateX, 0, 1, 0);
			GlStateManager.translate(-0.5f, -0.5f, -0.5f);
			DrawUtil.draw(builder);
			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();

		}
	}

	private void packManuell(ByteBuf buffer) {
		buffer.writeByte(GIRNetworkHandler.PLACEMENT_GUI_MANUELL_SET);

		buffer.writeInt(pos.getX());
		buffer.writeInt(pos.getY());
		buffer.writeInt(pos.getZ());

		buffer.writeInt(properties.size());
		properties.forEach((prop, id) -> {
			buffer.writeInt(block.getIDFromProperty(prop));
			buffer.writeInt(id);
		});
	}

	private void send() {
		ByteBuf buffer = Unpooled.buffer();
		switch (currentStage) {
		case MANUELL:
			packManuell(buffer);
			break;
		case REDSTONE:
			return;
		case SCRIPTING:
			return;
		default:
			return;
		}
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}

	@Override
	public void onGuiClosed() {
		send();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -200:
			send();
			break;
		default:
			break;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.lastX = mouseX;
		this.lastY = mouseY;
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		this.rotateX += mouseX - lastX;
		this.rotateY += mouseY - lastY;
		this.lastX = mouseX;
		this.lastY = mouseY;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}
