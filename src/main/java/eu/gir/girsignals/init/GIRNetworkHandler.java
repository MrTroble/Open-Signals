package eu.gir.girsignals.init;

import java.util.function.Consumer;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.EnumSignals.EnumMuxMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity.EnumRedstoneMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GIRNetworkHandler {

	public static final String CHANNELNAME = "gir|eventhandled";
	public static final String BLOCK_TYPE_ID = "blockid";
	public static final String SIGNAL_CUSTOMNAME = "customname";

	public static final byte PLACEMENT_GUI_SET_NBT = 0;
	public static final byte SIG_CON_GUI_MANUELL_SET = 1;
	public static final byte SIG_CON_RS_FACING_UPDATE_SET = 2;
	public static final byte SIG_CON_RS_SET = 3;
	public static final byte SIG_CON_SAVE_UI_STATE = 4;
	
	@SubscribeEvent
	public void onCustomPacket(ServerCustomPacketEvent event) {
		FMLProxyPacket packet = event.getPacket();
		ByteBuf payBuf = packet.payload();
		EntityPlayerMP mp = ((NetHandlerPlayServer) event.getHandler()).player;
		World world = mp.world;
		MinecraftServer server = mp.getServer();
		byte id = payBuf.readByte();
		switch (id) {
		case PLACEMENT_GUI_SET_NBT:
			readItemNBTSET(payBuf, mp);
			break;
		case SIG_CON_GUI_MANUELL_SET:
			readFromPos(payBuf, world, tile -> {
				final int type = payBuf.readInt();
				final int change = payBuf.readInt();
				server.addScheduledTask(() -> tile.changeSignalImpl(type, change));
			});
			break;
		case SIG_CON_RS_FACING_UPDATE_SET:
			readFromPos(payBuf, world, tile -> {
				final EnumFacing facing = EnumFacing.values()[payBuf.readInt()];
				final int data = payBuf.readInt();
				tile.setFacingData(facing, data);
			});
			break;
		case SIG_CON_RS_SET:
			readFromPos(payBuf, world, tile -> tile.setRsMode(EnumRedstoneMode.values()[payBuf.readInt()]));
			break;
		case SIG_CON_SAVE_UI_STATE:
			readFromPos(payBuf, world, tile -> tile.setUIState(EnumMode.values()[payBuf.readInt()], EnumFacing.values()[payBuf.readInt()], payBuf.readInt(), EnumMuxMode.values()[payBuf.readInt()]));
			break;
		default:
			throw new IllegalArgumentException("Wrong packet ID in network recive!");
		}
	}

	private static void readFromPos(final ByteBuf payBuf, final World world,
			final Consumer<SignalControllerTileEntity> consumer) {
		final BlockPos pos = new BlockPos(payBuf.readInt(), payBuf.readInt(), payBuf.readInt());
		final SignalControllerTileEntity tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		if (tile != null) {
			consumer.accept(tile);
		}
	}

	private static void readItemNBTSET(ByteBuf payBuf, EntityPlayer player) {
		int blockType = payBuf.readInt();
		int length = payBuf.readInt();
		byte[] strBuff = new byte[length];
		payBuf.readBytes(strBuff);
		String customName = new String(strBuff);
		Signal block = Signal.SIGNALLIST.get(blockType);
		ExtendedBlockState blockState = (ExtendedBlockState) block.getBlockState();
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setInteger(BLOCK_TYPE_ID, blockType);
		tagCompound.setString(SIGNAL_CUSTOMNAME, customName);
		for (IUnlistedProperty<?> property : blockState.getUnlistedProperties()) {
			SEProperty<?> prop = SEProperty.cst(property);
			if (prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				tagCompound.setBoolean(property.getName(), payBuf.readBoolean());
			} else if (prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				if (!prop.getType().equals(Boolean.class)) {
					tagCompound.setInteger(property.getName(), payBuf.readInt());
				} else {
					tagCompound.setBoolean(property.getName(), payBuf.readBoolean());
				}
			}
		}
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (stack.getItem() instanceof Placementtool) {
			stack.setTagCompound(tagCompound);
		}
	}

}
