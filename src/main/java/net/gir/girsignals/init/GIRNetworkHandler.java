package net.gir.girsignals.init;

import io.netty.buffer.ByteBuf;
import net.gir.girsignals.SEProperty;
import net.gir.girsignals.SEProperty.ChangeableStage;
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
	public static final String BLOCK_TYPE_ID = "blockid";

	@SubscribeEvent
	public void onCustomPacket(ServerCustomPacketEvent event) {
		FMLProxyPacket packet = event.getPacket();
		ByteBuf payBuf = packet.payload();
		int blockType = payBuf.readInt();
		SignalBlock block = SignalBlock.SIGNALLIST.get(blockType);
		ExtendedBlockState blockState = (ExtendedBlockState) block.getBlockState();
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setInteger(BLOCK_TYPE_ID, blockType);
		for (IUnlistedProperty<?> property : blockState.getUnlistedProperties()) {
			SEProperty<?> prop = SEProperty.cst(property);
			if(prop.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				tagCompound.setBoolean(property.getName(), payBuf.readBoolean());
			} else if(prop.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				tagCompound.setInteger(property.getName(), payBuf.readInt());
			}
		}

		EntityPlayer player = ((NetHandlerPlayServer) event.getHandler()).player;
		ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
		if (stack.getItem() instanceof Placementtool) {
			stack.setTagCompound(tagCompound);
		}
	}

}
