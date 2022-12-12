package com.troblecodings.signals.blocks;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;

public class RedstoneIO extends Block {

    public static final BooleanProperty POWER = BooleanProperty.create("power");

    public RedstoneIO() {
        super(Properties.of(Material.METAL));
        this.registerDefaultState(stateDefinition.any().setValue(POWER, false));
    }
    
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> p_49915_) {
    	this.stateDefinition = new StateDefinition<Block, BlockState>();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {
                POWER
        });
    }

    @Override
    public int getStrongPower(final BlockState blockState, final LevelAccessor blockAccess,
            final BlockPos pos, final Direction side) {
        return getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(final BlockState blockState, final LevelAccessor blockAccess,
            final BlockPos pos, final Direction side) {
        return blockState.getValue(POWER) ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(final Level worldIn, final int meta) {
        return new RedstoneIOTileEntity();
    }

    @Override
    public boolean onBlockActivated(final Level worldIn, final BlockPos pos,
            final BlockState state, final Player playerIn, final EnumHand hand,
            final Direction facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL)) {
            if (worldIn.isRemote)
                OpenSignalsMain.handler.invokeGui(RedstoneIO.class, playerIn, worldIn, pos);
            return true;
        }
        return false;
    }
}
