package com.troblecodings.signals.blocks;

import com.troblecodings.signals.init.GIRTabs;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class Post extends Block {

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
}