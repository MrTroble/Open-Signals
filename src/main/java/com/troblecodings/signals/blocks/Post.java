package com.troblecodings.signals.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Post extends BasicBlock {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(7 * 0.0625, 0.0, 7 * 0.0625,
            9 * 0.0625, 16 * 0.0625, 9 * 0.0625);

    public Post() {
        super(Material.ROCK);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }
}
