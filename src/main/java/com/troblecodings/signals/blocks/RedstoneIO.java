package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class RedstoneIO extends BasicBlock {

    public static final BooleanProperty POWER = BooleanProperty.create("power");
    public static final TileEntitySupplierWrapper SUPPLIER = RedstoneIOTileEntity::new;

    public RedstoneIO() {
        super(Properties.of(Material.METAL));
        this.registerDefaultState(stateDefinition.any().setValue(POWER, false));
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public int getSignal(final BlockState blockState, final BlockGetter world, final BlockPos pos,
            final Direction direction) {
        return this.getDirectSignal(blockState, world, pos, direction);
    }

    @Override
    public int getDirectSignal(final BlockState blockState, final BlockGetter world,
            final BlockPos pos, final Direction direction) {
        return blockState.getValue(POWER) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(final BlockState blockState) {
        return true;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        if (!playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem()
                .equals(OSItems.LINKING_TOOL)) {
            OpenSignalsMain.handler.invokeGui(RedstoneIO.class, playerIn, worldIn, pos,
                    "redstoneio");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }
    
    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("redstoneio");
    }
}
