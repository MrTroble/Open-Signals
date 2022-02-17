package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.init.GIRTabs;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.guilib.ecs.GuiHandler;
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

public class SignalController extends Block implements ITileEntityProvider {
	
	public SignalController() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.getHeldItemMainhand().getItem().equals(GIRItems.LINKING_TOOL)) {
			GuiHandler.invokeGui(Signal.class, playerIn, worldIn, pos);
			return true;
		}
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SignalControllerTileEntity();
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		final TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof SignalControllerTileEntity) {
			((SignalControllerTileEntity) tile).redstoneUpdate();
		}
	}
}
