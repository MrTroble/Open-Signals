package eu.gir.girsignals.init;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.SignalBlock;
import eu.gir.girsignals.items.Placementtool;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
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
	public static final byte PLACEMENT_GUI_MANUELL_SET = 1;

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
		case PLACEMENT_GUI_MANUELL_SET:
			readManuellSet(payBuf, world, server);
			break;
		default:
			throw new IllegalArgumentException("Wrong packet ID in network recive!");
		}
	}
	
	private static void readManuellSet(ByteBuf payBuf, World world, MinecraftServer server) {
		final BlockPos pos = new BlockPos(payBuf.readInt(), payBuf.readInt(), payBuf.readInt());
		final SignalControllerTileEntity tile = (SignalControllerTileEntity) world.getTileEntity(pos);
		if(tile != null) {
			int size = payBuf.readInt();
			for (int i = 0; i < size; i++) {
				final int type = payBuf.readInt();
				final int change = payBuf.readInt();
				server.addScheduledTask(() -> tile.changeSignalImpl(type, change));
			}
		}
	}
	
	private static void readItemNBTSET(ByteBuf payBuf, EntityPlayer player) {
		int blockType = payBuf.readInt();
		int length = payBuf.readInt();
		byte[] strBuff = new byte[length];
		payBuf.readBytes(strBuff);
		String customName = new String(strBuff);
		SignalBlock block = SignalBlock.SIGNALLIST.get(blockType);
		ExtendedBlockState blockState = (ExtendedBlockState) block.getBlockState();
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setInteger(BLOCK_TYPE_ID, blockType);
		tagCompound.setString(SIGNAL_CUSTOMNAME, customName);
		for (IUnlistedProperty<?> property : blockState.getUnlistedProperties()) {
			SEProperty<?> prop = SEProperty.cst(property);
			if(prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				tagCompound.setBoolean(property.getName(), payBuf.readBoolean());
			} else if(prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				tagCompound.setInteger(property.getName(), payBuf.readInt());
			}
		}
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (stack.getItem() instanceof Placementtool) {
			stack.setTagCompound(tagCompound);
		}
	}

}
