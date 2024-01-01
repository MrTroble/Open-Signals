package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.TrainNumberTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class TrainNumberBlock extends BasicBlock {

    public static final BooleanProperty POWERD = BooleanProperty.create("powerd");
    public static final TileEntitySupplierWrapper WRAPPER = TrainNumberTileEntity::new;

    public TrainNumberBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(POWERD);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos,
            final Block block, final BlockPos toPos, final boolean moveing) {
        if (world.isClientSide)
            return;
        if (world.hasNeighborSignal(pos)) {
            if (!state.getValue(POWERD)) {
                world.setBlockAndUpdate(pos, state.setValue(POWERD, true));
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TrainNumberTileEntity) {
                    ((TrainNumberTileEntity) entity).updateTrainNumberViaRedstone();
                }
            }
        } else {
            world.setBlockAndUpdate(pos, state.setValue(POWERD, false));
        }
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult result) {
        final Item item = player.getMainHandItem().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) && item.equals(OSItems.MULTI_LINKING_TOOL))) {
            OpenSignalsMain.handler.invokeGui(TrainNumberBlock.class, player, world, pos,
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
        return Optional.of("trainnumberchanger");
    }

}