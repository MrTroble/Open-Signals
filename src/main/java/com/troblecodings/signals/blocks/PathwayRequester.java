package com.troblecodings.signals.blocks;


import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.PathwayRequesterTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class PathwayRequester extends BasicBlock {

    public static final BooleanProperty POWERD = BooleanProperty.create("powerd");
    public static final TileEntitySupplierWrapper WRAPPER = PathwayRequesterTileEntity::new;

    public PathwayRequester() {
        super(Properties.of(Material.METAL));
    }
    
    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(POWERD);
    }

    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos,
            final Block block, final BlockPos toPos, final boolean moveing) {
        if (world.isClientSide)
            return;
        if (world.hasNeighborSignal(pos)) {
            if (!state.getValue(POWERD)) {
                world.setBlockAndUpdate(pos, state.setValue(POWERD, true));
                final TileEntity entity = world.getBlockEntity(pos);
                if (entity instanceof PathwayRequesterTileEntity) {
                    ((PathwayRequesterTileEntity) entity).requestPathway();
                }
            }
        } else {
            world.setBlockAndUpdate(pos, state.setValue(POWERD, false));
        }
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult result) {
        final Item item = player.getMainHandItem().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) && item.equals(OSItems.MULTI_LINKING_TOOL))) {
            OpenSignalsMain.handler.invokeGui(PathwayRequester.class, player, world, pos,
                    "pathwayrequester");
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
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
