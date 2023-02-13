package com.troblecodings.signals.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Post extends BasicBlock {

    private static final VoxelShape BOUNDING_BOX = Shapes.box(7 * 0.0625, 0.0, 7 * 0.0625,
            9 * 0.0625, 16 * 0.0625, 9 * 0.0625);

    public Post() {
        super(Properties.of(Material.METAL));
    }

    @Override
    public VoxelShape getShape(final BlockState p_60555_, final BlockGetter p_60556_,
            final BlockPos p_60557_, final CollisionContext p_60558_) {
        return BOUNDING_BOX;
    }
}
