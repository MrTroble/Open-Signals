package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.tileentitys.DisplayTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Display extends BasicBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final TileEntitySupplierWrapper SUPPLIER = DisplayTileEntity::new;

    private final boolean isDoubleSided;

    public Display(final boolean isDoubleSided) {
        super(Properties.of(Material.STONE));
        this.isDoubleSided = isDoubleSided;
        this.registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING,
                ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(final BlockState state, final Level level, final BlockPos pos,
            final BlockState newState, final boolean flag) {
        BlockPos rightBlock;
        switch (state.getValue(FACING)) {
            case NORTH:
                rightBlock = pos.west();
                break;
            case EAST:
                rightBlock = pos.north();
                break;
            case SOUTH:
                rightBlock = pos.east();
                break;
            case WEST:
                rightBlock = pos.south();
                break;
            default:
                rightBlock = pos;
                break;
        }
        // TODO Place Ghostblock
        // level.setBlockAndUpdate(rightBlock,
        // OSBlocks.GHOST_BLOCK.defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info) {
        final float offsetX = -10;
        final float offsetZ = 35;
        final int angleMod;
        final float xMod;
        final float zMod;
        final float doubleXMod;
        final float doubleZMod;
        BlockEntity te = info.tileEntity;
        final BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        if (!(state.getBlock() instanceof Display))
            return;
        switch (state.getValue(FACING)) {
            case NORTH:
                angleMod = 2;
                zMod = -70;
                xMod = -50;
                doubleZMod = -70;
                doubleXMod = -120;
                break;
            case EAST:
                angleMod = 3;
                zMod = -10;
                xMod = -60;
                doubleZMod = -72;
                doubleXMod = -115;
                break;
            case WEST:
                angleMod = 1;
                zMod = -63;
                xMod = 11;
                doubleZMod = -70;
                doubleXMod = -120;
                break;
            default:
                angleMod = 0;
                zMod = 0;
                xMod = 0;
                doubleZMod = -71;
                doubleXMod = -110;
                break;
        }

        info.push();
        info.translate(info.x, info.y + 0.95f, info.z + 0.5f);
        info.scale(-0.015f, 0.015f, 0.015f);
        info.translate(offsetX + xMod, 0, offsetZ + zMod);
        info.scale(-1f, 1f, 1f);
        info.rotate((float) Math.PI, 0, 0);
        info.rotate(0, (float) Math.PI * 0.5f * angleMod, 0);
        // this.drawColoredString(info.font, line_1, colorDirectionX, colorDirectionZ);
        info.font.draw(info.stack, "Line1", 0, 0, 0xFFFFFFFF);
        info.translate(0, 10, 0);
        // this.drawColoredString(font, line_2, colorDirectionX, colorDirectionZ);
        info.font.draw(info.stack, "Line2", 0, 0, 0xFFFFFFFF);
        info.translate(0, 10, 0);
        // this.drawColoredString(font, line_3, colorDirectionX, colorDirectionZ);
        info.font.draw(info.stack, "Line3", 0, 0, 0xFFFFFFFF);

        if (this.isDoubleSided) {
            info.rotate(0, (float) Math.PI, 0);
            info.translate(doubleXMod, 0, doubleZMod);
            info.font.draw(info.stack, "Line1", 0, 0, 0xFFFFFFFF);
            info.translate(0, -10, 0);
            // font.drawString(line_2, 0, 0, 0xFFFFFFFF);
            info.font.draw(info.stack, "Line2", 0, 0, 0xFFFFFFFF);
            info.translate(0, -10, 0);
            // font.drawString(line_1, 0, 0, 0xFFFFFFFF);
            info.font.draw(info.stack, "Line3", 0, 0, 0xFFFFFFFF);
        }
        info.pop();
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("display");
    }

}
