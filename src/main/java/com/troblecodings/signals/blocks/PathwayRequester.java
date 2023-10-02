package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class PathwayRequester extends BasicBlock {

    public static final BooleanProperty POWERD = BooleanProperty.create("powerd");
    public static final TileEntitySupplierWrapper WRAPPER = PathwayRequesterTileEntity::new;

    public PathwayRequester() {
        super(Properties.of(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERD);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block,
            BlockPos toPos, boolean moveing) {
        if (world.isClientSide)
            return;
        if (world.hasNeighborSignal(pos)) {
            if (!state.getValue(POWERD)) {
                world.setBlockAndUpdate(pos, state.setValue(POWERD, true));
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof PathwayRequesterTileEntity) {
                    ((PathwayRequesterTileEntity) entity).requestPathway();
                }
            }
        } else {
            world.setBlockAndUpdate(pos, state.setValue(POWERD, false));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult result) {
        if (player.getMainHandItem().getItem().equals(OSItems.MANIPULATOR)) {
            OpenSignalsMain.handler.invokeGui(PathwayRequester.class, player, world, pos,
                    "pathwayrequester");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(WRAPPER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("pathwayrequeseter");
    }
}