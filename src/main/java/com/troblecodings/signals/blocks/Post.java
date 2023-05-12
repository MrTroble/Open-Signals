package com.troblecodings.signals.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class Post extends BasicBlock {

    private static final VoxelShape BOUNDING_BOX = VoxelShapes.box(7 * 0.0625, 0.0, 7 * 0.0625,
            9 * 0.0625, 16 * 0.0625, 9 * 0.0625);

    public Post() {
        super(Properties.of(Material.METAL));
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader getter,
            final BlockPos pos, final ISelectionContext context) {
        return BOUNDING_BOX;
    }
}
