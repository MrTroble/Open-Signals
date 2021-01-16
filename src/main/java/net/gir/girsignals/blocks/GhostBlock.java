package net.gir.girsignals.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GhostBlock extends Block {

	public GhostBlock() {
		super(Material.GLASS);
		setLightLevel(0);
	}

	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}

	@Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.INVISIBLE;
    }

    @SideOnly(Side.CLIENT)
	@Override
    public float getAmbientOcclusionLightValue(IBlockState state)
    {
        return 1.0F;
    }

	@Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
			EntityPlayer player) {
		return SignalBlock.pickBlock(player);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT;
	}

	public static void destroyUpperBlock(World worldIn, BlockPos pos) {
		BlockPos posup = pos.up();
		Block upperBlock = worldIn.getBlockState(posup).getBlock();
		if (upperBlock instanceof GhostBlock) {
			worldIn.destroyBlock(posup, false);
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);

		destroyUpperBlock(worldIn, pos);

		BlockPos posdown = pos.down();
		Block lowerBlock = worldIn.getBlockState(posdown).getBlock();
		if (lowerBlock instanceof GhostBlock || lowerBlock instanceof SignalBlock) {
			worldIn.destroyBlock(posdown, false);
		}
	}
}
