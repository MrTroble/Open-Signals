package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.init.GIRTabs;
import eu.gir.girsignals.linkableApi.Linkingtool;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBox extends Block implements ITileEntityProvider {

	public SignalBox() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SignalBoxTileEntity();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(playerIn.getHeldItemMainhand().getItem() instanceof Linkingtool)
			return false;
		playerIn.openGui(GirsignalsMain.MODID, EnumSignals.GUI_SIGNAL_BOX, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

}
