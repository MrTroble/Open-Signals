package eu.gir.girsignals.blocks;

import eu.gir.girsignals.init.SignalTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class Post extends Block {

    public Post() {
        super(Material.ROCK);
        setCreativeTab(SignalTabs.TAB);
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
