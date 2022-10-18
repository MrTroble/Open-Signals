package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Post extends Block {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(7 * 0.0625, 0.0, 7 * 0.0625,
            9 * 0.0625, 16 * 0.0625, 9 * 0.0625);

    public Post() {
        super(Material.ROCK);
        setCreativeTab(GIRTabs.TAB);
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        return BOUNDING_BOX;
    }
}
