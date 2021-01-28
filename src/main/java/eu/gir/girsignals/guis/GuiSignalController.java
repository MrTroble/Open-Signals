package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

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
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiScreen
		implements IIntegerable<GuiSignalController.Stages>, Consumer<Integer> {

	private GUISettingsSlider slider;
	private final SignalBlock block;
	private final SignalControllerTileEntity tile;
	private final HashMap<SEProperty<?>, Integer> properties = new HashMap<>();
	private final BlockPos pos;
	
	public GuiSignalController(BlockPos pos, World world) {
		this.pos = pos;
		this.tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		if(!this.tile.hasLinkImpl()) {
			this.block = null;
			return;
		}
		IBlockState state = world.getBlockState(this.tile.getLinkedPosition());
		this.block = (SignalBlock) state.getBlock();
		
	}

	public static enum Stages {
		MANUELL, REDSTONE, SCRIPTING
	}

	private Stages currentStage = Stages.MANUELL;

	@Override
	public void initGui() {
		this.slider = new GUISettingsSlider(this, -100, (this.width - 150) / 2, 10, 150,
				"stagetype", this.slider != null ? this.slider.getValue():0, this);
		
		if(block == null) {
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
		
		GuiButton apply = new GuiButton(-200, (this.width - 150) / 2, this.height - 50, "");
		addButton(apply);
	}

	private void initManuell() {
		int maxWidth = 0;

		for (int x : tile.getSupportedSignalTypesImpl()) {
			SEProperty<?> prop = (SEProperty<?>) block.getPropertyFromID(x);
			String format = I18n.format("property." + prop.getName() + ".name");
			int maxProp = 0;
			if(prop.getType().isEnum()) {
				for (int i = 0; i < prop.count(); i++) {
					maxProp = Math.max(fontRenderer.getStringWidth(String.format(prop.getObjFromID(i).toString())), maxProp);
				}
			}
			maxWidth = Math.max(fontRenderer.getStringWidth(format) + maxProp, maxWidth);
			properties.put(prop, tile.getSignalStateImpl(block.getIDFromProperty(prop)));
		}

		int y = 30;
		int x = 30;
		for (Entry<SEProperty<?>, Integer> entry : properties.entrySet()) {
			SEProperty<?> prop = entry.getKey();
			this.addButton(new GUISettingsSlider(prop, 0, x, (y += 30), maxWidth, prop.getName(), entry.getValue().intValue(), in -> properties.put(prop, in)));
			if (y >= 250) {
				y = 30;
				x += maxWidth + 20;
			}
		}
	}

	private void initRedstone() {
		// TODO Add redstone support
	}

	private void initScripting() {
		// TODO Add scripting
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
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -200:
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
			break;

		default:
			break;
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void accept(Integer t) {
		currentStage = getObjFromID(t);
		this.buttonList.clear();
		initGui();
	}

	@Override
	public Stages getObjFromID(int obj) {
		return Stages.values()[obj];
	}

	@Override
	public int count() {
		return Stages.values().length;
	}

}
