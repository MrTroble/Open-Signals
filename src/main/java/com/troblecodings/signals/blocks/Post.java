package com.troblecodings.signals.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Post extends BasicBlock {

    private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(7 * 0.0625, 0.0, 7 * 0.0625,
            9 * 0.0625, 16 * 0.0625, 9 * 0.0625);

    public Post() {
        super(Properties.of(Material.METAL));
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_,
            BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        // TODO Auto-generated method stub
        return super.getShape(p_220053_1_, p_220053_2_, p_220053_3_, p_220053_4_);
    }
}
