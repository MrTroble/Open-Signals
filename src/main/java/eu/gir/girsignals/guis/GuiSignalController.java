package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiScreen {

	private final SignalControllerTileEntity tile;
	private final HashMap<SEProperty<?>, Integer> properties = new HashMap<>();
	private final BlockPos pos;
	private final World world;

	private GUIEnumerableSetting slider;
	private Signal block;
	private BufferBuilder builder = null;

	public GuiSignalController(BlockPos pos, World world) {
		this.pos = pos;
		this.world = world;
		this.tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		Arrays.fill(RSMODES, RedstoneMode.SINGEL);
		this.tile.onLink();
		this.tile.loadChunkAndGetTile((tile, ch) -> {
			this.block = Signal.SIGNALLIST.get(tile.getBlockID());
		});
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
		this.slider = new GUIEnumerableSetting(enumToInt, -100, (this.width - 150) / 2, 10, 150, "stagetype",
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

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

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

}
