package net.gir.girsignals.init;

import io.netty.buffer.ByteBuf;
import net.gir.girsignals.blocks.SignalBlock;
import net.gir.girsignals.items.Placementtool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GIRNetworkHandler {

	public static final String CHANNELNAME = "gir|setItemNBT";

	@SubscribeEvent
	public void onCustomPacket(ServerCustomPacketEvent event) {
		FMLProxyPacket packet = event.getPacket();
		ByteBuf payBuf = packet.payload();
		int blockType = payBuf.getInt(0);
		SignalBlock block = SignalBlock.SIGNALLIST.get(blockType);
		ExtendedBlockState blockState = (ExtendedBlockState) block.getBlockState();
		NBTTagCompound tagCompound = new NBTTagCompound();
		int n = 1;
		for (IUnlistedProperty<?> property : blockState.getUnlistedProperties()) {
			tagCompound.setBoolean(property.getName(), payBuf.getBoolean(n++));
		}

		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).player;
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (stack.getItem() instanceof Placementtool) {
			stack.setTagCompound(tagCompound);
			System.out.println(tagCompound);
		}
	}

}
