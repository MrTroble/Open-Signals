package net.gir.girsignals.items;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.init.GIRTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Placementtool extends Item {

	public Placementtool() {
		setCreativeTab(GIRTabs.tab);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			player.openGui(GirsignalsMain.MODID, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return EnumActionResult.PASS;

	}

}
