package com.troblecodings.signals.tileentitys;

import com.troblecodings.signals.blocks.Display;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DisplayTileEntity extends SyncableTileEntity
        implements BlockEntityTicker<DisplayTileEntity> {

    public static final int TICS_PER_SECOND = 20;
    public static final int FETCH_INTERVAL_IN_TICKS =
            ConfigHandler.GENERAL.displayFetchInterval.get() * TICS_PER_SECOND;

    private int timeSinceLastFetch;

    public DisplayTileEntity(final TileEntityInfo info) {
        super(info);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info) {
        Block display = getBlockState().getBlock();
        if (display == null || !(display instanceof Display))
            return;
        ((Display) display).renderOverlay(info.with(this));
    }

    public void fetchStationData() {

    }

    @Override
    public void tick(final Level world, final BlockPos pos, final BlockState state,
            final DisplayTileEntity tile) {
        if (world.isClientSide)
            return;
        if (this.timeSinceLastFetch >= FETCH_INTERVAL_IN_TICKS) {
            this.fetchStationData();
            this.timeSinceLastFetch = 0;
        } else {
            this.timeSinceLastFetch++;
        }
    }

}
