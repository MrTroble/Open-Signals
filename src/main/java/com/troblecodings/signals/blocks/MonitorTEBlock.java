package com.troblecodings.signals.blocks;

import java.util.Optional;

import org.lwjgl.opengl.GL11;

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

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MonitorTEBlock extends Monitor {

    public static final TileEntitySupplierWrapper SUPPLIER = MonitorTileEntity::new;
    public static final float STEPS_PER_BLOCK = 50;

    public MonitorTEBlock(final MonitorBlockProperties prop) {
        super(prop, null);
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source,
            final BlockPos pos) {
        final MonitorTileEntity tile = (MonitorTileEntity) source.getTileEntity(pos);
        if (tile == null)
            return FULL_BLOCK_AABB;
        final EnumFacing direction = state.getValue(FACING).rotateY();
        final Vec3i vec = direction.getDirectionVec();
        return FULL_BLOCK_AABB.expand(-vec.getX() * (tile.getMonitorSizeX() - 1),
                tile.getMonitorSizeY() - 1, -vec.getZ() * (tile.getMonitorSizeX() - 1));
    }

    public void render(final RenderAnimationInfo info, final MonitorTileEntity tile,
            final UISignalBoxRendering rendering) {
        final float monitorSizeX = tile.getMonitorSizeX();
        final float monitorSizeY = tile.getMonitorSizeY();
        final float renderSizeX = tile.getRenderEnd().getX() - tile.getRenderStart().getX() + 1f;
        final float renderSizeY = tile.getRenderEnd().getY() - tile.getRenderStart().getY() + 1f;

        final float insets = getMonitorProperties().getInsets();
        final float colorInsets = (insets - 6) / STEPS_PER_BLOCK;

        final DrawInfo drawInfo = new DrawInfo(0, 0, info.tick);
        drawInfo.push();
        GlStateManager.translate(info.x, info.y, info.z);

        rotate(drawInfo, tile);

        drawInfo.push();
        GlStateManager.translate(-monitorSizeX + 1, 0, -0.001f);

        drawInfo.disableTexture();
        drawInfo.applyColor();
        drawInfo.blendOn();
        drawInfo.alphaOn();
        GlStateManager.disableLighting();

        final BufferWrapper wrapper =
                drawInfo.builder(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        wrapper.quad(colorInsets, (monitorSizeX - colorInsets), colorInsets,
                monitorSizeY - colorInsets, tile.getProfile().getBackgroundColor());
        drawInfo.end();
        drawInfo.pop();

        GlStateManager.translate(1, monitorSizeY, 0);
        GlStateManager.rotate(2 * UIRotate.PERPENDICULAR_ANGLE, 0, 0, 1);
        GlStateManager.scale(1 / STEPS_PER_BLOCK, 1 / STEPS_PER_BLOCK, 1 / STEPS_PER_BLOCK);
        GlStateManager.translate(insets, insets, -0.35f);

        final float maxSizeX = monitorSizeX * STEPS_PER_BLOCK - 2 * insets;
        final float maxSizeY = monitorSizeY * STEPS_PER_BLOCK - 2 * insets;
        GlStateManager.scale(maxSizeX / (renderSizeX * UISignalBoxRendering.TILE_WIDTH),
                maxSizeY / (renderSizeY * UISignalBoxRendering.TILE_WIDTH), 1);

        GlStateManager.scale(1, 1, -0.1f);
        rendering.draw(drawInfo);

        drawInfo.alphaOff();
        drawInfo.blendOff();
        drawInfo.enableTexture();
        drawInfo.depthOn();
        GlStateManager.enableLighting();
        drawInfo.pop();
    }

    private static void rotate(final DrawInfo info, final MonitorTileEntity tile) {
        final IBlockState state = tile.getWorld().getBlockState(tile.getPos());
        final EnumFacing direction = state.getValue(FACING);
        if (direction.equals(EnumFacing.DOWN) || direction.equals(EnumFacing.UP)
                || direction.equals(EnumFacing.NORTH))
            return;

        GlStateManager.translate(0.5f, 0, 0.5f);
        switch (direction) {
            case EAST:
                GlStateManager.rotate(3 * UIRotate.PERPENDICULAR_ANGLE, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(2 * UIRotate.PERPENDICULAR_ANGLE, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(UIRotate.PERPENDICULAR_ANGLE, 0, 1, 0);
                break;
            default:
                break;
        }
        GlStateManager.translate(-0.5f, 0, -0.5f);
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        final Item item = playerIn.getHeldItemMainhand().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) || item.equals(OSItems.MULTI_LINKING_TOOL))) {
            OpenSignalsMain.handler.invokeGui(MonitorTEBlock.class, playerIn, worldIn, pos,
                    "monitorTE");
            return true;
        }
        return false;
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