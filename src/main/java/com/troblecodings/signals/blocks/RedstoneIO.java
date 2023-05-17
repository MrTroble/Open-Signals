package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.NameStateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        final World world = context.getLevel();
        if (!world.isClientSide) {
            NameHandler.setNameForNonSignals(new NameStateInfo(world, context.getClickedPos()),
                    this.getRegistryName().getPath());
        }
        return this.defaultBlockState();
    }

    @Override
    public int getSignal(final BlockState blockState, final IBlockReader world, final BlockPos pos,
            final Direction direction) {
        return this.getDirectSignal(blockState, world, pos, direction);
    }

    @Override
    public int getDirectSignal(final BlockState state, final IBlockReader getter,
            final BlockPos pos, final Direction direction) {
        return state.getValue(POWER) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(final BlockState blockState) {
        return true;
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult result) {
        if (!player.getItemInHand(Hand.MAIN_HAND).getItem().equals(OSItems.LINKING_TOOL)) {
            OpenSignalsMain.handler.invokeGui(RedstoneIO.class, player, world, pos, "redstoneio");
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("redstoneio");
    }

    @Override
    public void destroy(final IWorld acess, final BlockPos pos, final BlockState state) {
        super.destroy(acess, pos, state);
        if (!acess.isClientSide()) {
            NameHandler.setRemoved(new NameStateInfo((World) acess, pos));
            SignalBoxHandler.onPosRemove(new PosIdentifier(pos, (World) acess));
        }
    }
}
