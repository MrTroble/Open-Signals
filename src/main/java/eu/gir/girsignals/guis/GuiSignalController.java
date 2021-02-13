package eu.gir.girsignals.guis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

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
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiScreen implements GuiResponder {

	private GUISettingsSlider slider;
	private final SignalBlock block;
	private final SignalControllerTileEntity tile;
	private final HashMap<SEProperty<?>, Integer> properties = new HashMap<>();
	private final BlockPos pos;
	private final World world;
	private final IBlockState state;

	public GuiSignalController(BlockPos pos, World world) {
		this.pos = pos;
		this.world = world;
		this.tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		Arrays.fill(RSMODES, RedstoneMode.SINGEL);
		if (!this.tile.hasLinkImpl()) {
			this.state = null;
			this.block = null;
			return;
		}		
		this.tile.onLink();
		this.state = world.getBlockState(this.tile.getLinkedPosition());
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

		if (builder == null) {
			builder = new BufferBuilder(50);
			BlockModelShapes shapes = mc.getBlockRendererDispatcher().getBlockModelShapes();

			builder.reset();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			builder.setTranslation(0, 0, 0);
			DrawUtil.addToBuffer(builder, shapes, world.getBlockState(pos));

			for (EnumFacing face : EnumFacing.VALUES) {
				BlockPos curPos = pos.offset(face);
				IBlockState state2 = world.getBlockState(curPos);
				if (state2 != null && !state2.getBlock().isAir(state2, world, curPos)) {
					Vec3i dirVec = face.getDirectionVec();
					builder.setTranslation(dirVec.getX(), dirVec.getY(), dirVec.getZ());
					DrawUtil.addToBuffer(builder, shapes, state2, 0x1FFFFFFF);
				}
			}
			builder.finishDrawing();
		}

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

	private RedstoneMode[] RSMODES = new RedstoneMode[EnumFacing.VALUES.length];

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

	private BufferBuilder builder = null;

	private void initRedstone() {
	}

	private void initScripting() {
		// TODO Add scripting
	}

	private float rotateY = 0, rotateX = 0, lastX = 0, lastY = 0, amountScrolled = 0;

	private void test(int mouseX, int mouseY) {
		double ver = this.width/(float)this.height;
		double d1 = (mouseX / (float)this.width) -0.5;
		double d2 = (mouseY / (float)this.height) * ver -0.5;
		Vec3d pvec2 = new Vec3d(d1, d2, -10);
		Vec3d pvec3 = new Vec3d(d1, d2, 10);
		RayTraceResult result = state.collisionRayTrace(world, pos, pvec2.add(new Vec3d(pos)), pvec3.add(new Vec3d(pos)));
		if(result != null) {
			drawGradientRect(mouseX, mouseY, mouseX + 4, mouseY + 4, 0xFFFFFFFF, 0xFFFFFFFF);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (currentStage == Stages.REDSTONE) {
			mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			
			GlStateManager.enableRescaleNormal();
			GlStateManager.pushMatrix();
			for (int i = 0; i < this.width; i++) {
				for (int j = 0; j < this.height; j++) {
					test(i, j);
				}
			}

			GlStateManager.translate((float) this.width * 0.5f, (float) this.height * 0.5f, 100);
			float scale = 32.0F + amountScrolled;
			GlStateManager.scale(scale, -scale, scale);
			GlStateManager.rotate(rotateY, 1, 0, 0);
			GlStateManager.rotate(rotateX, 0, 1, 0);
			GlStateManager.translate(-0.5f, -0.5f, -0.5f);
			
			FloatBuffer floatbuff = ByteBuffer.allocateDirect(16*4).asFloatBuffer();
			GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, floatbuff);
			
			GlStateManager.scale(0.01, 0.01, 0.01);
			GuiUtils.drawGradientRect(101, 0, 100, 100, 0, 0x1F0000FF, 0x1F0000FF);
			GlStateManager.scale(100, 100, 100);
			
	        GlStateManager.shadeModel(GL11.GL_SMOOTH);
	        GlStateManager.enableBlend();
			DrawUtil.draw(builder);

			Matrix4f matrix = new Matrix4f();
			//matrix.load(floatbuff);
			matrix.setIdentity();
			//matrix.scale(new Vector3f(scale, -scale, scale));
			matrix.rotate(rotateY, new Vector3f(1, 0, 0));			
			matrix.rotate(rotateX, new Vector3f(0, 1, 0));
						
			Vector4f pvec = new Vector4f((mouseX / (float)this.width) - 0.5f, (mouseY / (float)this.height) - 0.5f, 10, 1);
			
			for (EnumFacing face : EnumFacing.VALUES) {
				Vec3i vec = face.getDirectionVec();
				Vector4f spann1 = new Vector4f(vec.getX() == 0 ? 1000:0, vec.getY() == 0 ? 1000:0, vec.getZ() == 0 ? 1000:0, 1);
				Vector4f normal = Matrix4f.transform(matrix, new Vector4f(vec.getX(), vec.getY(), vec.getZ(), 1), null);
				Matrix4f.transform(matrix, spann1, spann1);
				float b = Vector4f.dot(spann1, normal);
				Vector4f vec2 = new Vector4f(mouseX/width, mouseY/height, 100, 1);
				Matrix4f.transform(matrix, vec2, vec2);
				//System.out.println(face);
				//System.out.println();
			}
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

	@Override
	public void setEntryValue(int id, boolean value) {
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
	}

}
