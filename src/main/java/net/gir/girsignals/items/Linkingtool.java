package net.gir.girsignals.items;

import net.gir.girsignals.controllers.SignalController;
import net.gir.girsignals.controllers.SignalControllerTileEntity;
import net.gir.girsignals.init.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Linkingtool extends Item {

	public Linkingtool() {
		setCreativeTab(Tabs.tab);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(worldIn.isRemote)
			return EnumActionResult.PASS;
		Block block = worldIn.getBlockState(pos).getBlock();
		if (block instanceof BlockDirt) { // TODO Change this to the actual Signal Block
			NBTTagCompound comp = new NBTTagCompound();
			SignalControllerTileEntity.writeBlockPosToNBT(pos, comp);
			player.getHeldItem(hand).setTagCompound(comp);
			player.sendMessage(new TextComponentTranslation("lt.added.block.pos", pos.toString()));
			return EnumActionResult.SUCCESS;
		} else if(block instanceof SignalController) {
			if(((SignalControllerTileEntity)worldIn.getTileEntity(pos)).link(player.getHeldItem(hand)))
				player.sendMessage(new TextComponentTranslation("lt.set.block.pos", pos.toString()));
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

}
