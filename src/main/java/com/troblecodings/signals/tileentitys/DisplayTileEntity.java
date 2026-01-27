package com.troblecodings.signals.tileentitys;

import com.troblecodings.signals.blocks.Display;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntityInfo;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DisplayTileEntity extends SyncableTileEntity {

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

}
