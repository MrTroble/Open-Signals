package net.gir.girsignals.init;

import io.netty.buffer.ByteBuf;
import net.gir.girsignals.blocks.SignalBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.CustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber
public class GIRNetworkHandler {

	public static final String CHANNELNAME = "gir|setItemNBT";
	
	@SideOnly(Side.SERVER)
	@SubscribeEvent()
	public static void onCustomPacket(CustomPacketEvent<NetHandlerPlayServer> event) {
		
		FMLProxyPacket packet = event.getPacket();
		if (packet.channel().equals(CHANNELNAME)) {
			System.out.print("Das Packet ist auf dem Channel " + CHANNELNAME + " angekommen!");
			ByteBuf payBuf = packet.payload();
			int blockType = payBuf.getInt(0);
			SignalBlock block = SignalBlock.SIGNALLIST.get(blockType);
			ExtendedBlockState blockState = (ExtendedBlockState) block.getBlockState();
			NBTTagCompound tagCompound = new NBTTagCompound();
			int n = 1;
			for (IUnlistedProperty<?> property : blockState.getUnlistedProperties()) {
				tagCompound.setBoolean(property.getName(), payBuf.getBoolean(n++));
			}

			EntityPlayer player = event.getHandler().player;
			player.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
		}
	}

}
