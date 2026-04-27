package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.troblecodings.guilib.ecs.entitys.BufferWrapper;
import com.troblecodings.guilib.ecs.entitys.DrawInfo;
import com.troblecodings.guilib.ecs.entitys.transform.UIRotate;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.MonitorBlockProperties;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.MonitorTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MonitorTEBlock extends Monitor {

    public static final TileEntitySupplierWrapper SUPPLIER = MonitorTileEntity::new;

    public MonitorTEBlock(final MonitorBlockProperties prop) {
        super(prop, null);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final CollisionContext context) {
        final MonitorTileEntity tile = (MonitorTileEntity) getter.getBlockEntity(pos);
        if (tile == null)
            return Shapes.block();
        return Shapes.create(Shapes.block().bounds().expandTowards(tile.getMonitorSizeX() - 1,
                tile.getMonitorSizeY() - 1, 0));
    }

    public void render(final RenderAnimationInfo info, final MonitorTileEntity tile,
            final UISignalBoxRendering rendering) {
        final float monitorSizeX = tile.getMonitorSizeX();
        final float monitorSizeY = tile.getMonitorSizeY();
        final float renderSizeX = tile.getRenderEnd().getX() - tile.getRenderStart().getX() + 1f;
        final float renderSizeY = tile.getRenderEnd().getY() - tile.getRenderStart().getY() + 1f;
        final DrawInfo drawInfo = new DrawInfo(info.stack);
        drawInfo.push();
        RenderSystem.depthMask(true);
        drawInfo.alphaOn();
        drawInfo.blendOn();
        drawInfo.depthOff();
        drawInfo.applyColor();

        final float stepsPerBlock = 100;

        final float insets = 12;
        final float colorInsets = insets / stepsPerBlock;
        drawInfo.push();
        drawInfo.depthOn();
        drawInfo.translate(-monitorSizeX + 1, 0, -0.001f);
        final BufferWrapper wrapper =
                drawInfo.builder(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        wrapper.quad(colorInsets, (monitorSizeX - colorInsets), colorInsets,
                monitorSizeY - colorInsets, tile.getProfile().getBackgroundColor());
        drawInfo.end();
        drawInfo.pop();

        drawInfo.translate(1, 1.05f * monitorSizeY, 0);
        drawInfo.rotate(0, 0, 2 * UIRotate.PERPENDICULAR_ANGLE);

        drawInfo.scale(1 / stepsPerBlock, 1 / stepsPerBlock, 1 / stepsPerBlock);
        drawInfo.translate(insets, insets, -0.35f);

        final float maxSizeX = monitorSizeX * stepsPerBlock - 2 * insets;
        final float maxSizeY = monitorSizeY * stepsPerBlock - 2 * insets;
        drawInfo.scale(maxSizeX / (renderSizeX * UISignalBoxRendering.TILE_WIDTH),
                maxSizeY / (renderSizeY * UISignalBoxRendering.TILE_WIDTH), 1);

        drawInfo.scale(1, 1, -0.1f);
        rendering.draw(drawInfo);
        drawInfo.blendOff();
        drawInfo.depthOff();
        drawInfo.alphaOff();
        drawInfo.pop();
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        if (!playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem()
                .equals(OSItems.LINKING_TOOL)) {
            OpenSignalsMain.handler.invokeGui(MonitorTEBlock.class, playerIn, worldIn, pos,
                    "monitorTE");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public MonitorTEBlock getTileEntityMonitorBlock() {
        return this;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("monitor.tile");
    }

}
