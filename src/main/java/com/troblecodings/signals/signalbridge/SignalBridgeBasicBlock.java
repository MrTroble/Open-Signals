package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.DestroyHelper;
import com.troblecodings.signals.enums.SignalBridgeType;
import com.troblecodings.signals.init.OSItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class SignalBridgeBasicBlock extends BasicBlock {

    public static final DirectionProperty FACING = HorizontalBlock.FACING;
    public static final List<SignalBridgeBasicBlock> ALL_SIGNALBRIDGE_BLOCKS = new ArrayList<>();

    private final SignalBridgeBlockProperties properties;
    // Just for Networking
    private final int id;

    public SignalBridgeBasicBlock(final SignalBridgeBlockProperties properties) {
        super(Properties.of(Material.STONE).noOcclusion()
                .lightLevel(u -> ConfigHandler.GENERAL.lightEmission.get())
                .isRedstoneConductor((_u1, _u2, _u3) -> false));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.properties = properties;
        this.id = ALL_SIGNALBRIDGE_BLOCKS.size();
        ALL_SIGNALBRIDGE_BLOCKS.add(this);
    }

    public int getID() {
        return id;
    }

    public SignalBridgeType getType() {
        return properties.getType();
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotaion) {
        return state.setValue(FACING, rotaion.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean shouldHaveItem() {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(final IBlockReader reader, final BlockPos pos,
            final BlockState state) {
        return new ItemStack(OSItems.SIGNAL_BRIDGE_ITEM);
    }

    @Override
    public void destroy(final IWorld acess, final BlockPos pos, final BlockState state) {
        super.destroy(acess, pos, state);
        DestroyHelper.checkAndDestroyOtherBlocks(acess, pos, state,
                block -> block instanceof SignalBridgeBasicBlock || block instanceof Signal
                        || block instanceof GhostBlock);
    }
}