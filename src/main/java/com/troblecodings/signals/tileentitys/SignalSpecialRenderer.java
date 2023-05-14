package com.troblecodings.signals.tileentitys;

import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class SignalSpecialRenderer extends TileEntityRenderer<SignalTileEntity> {

    private final TileEntityRendererDispatcher context;

    public SignalSpecialRenderer(final TileEntityRendererDispatcher context) {
        super();
        this.context = context;
    }
    
    @Override
    public void render(final SignalTileEntity tile, final double x, final double y,
            final double z, final float tick, final int destroyStage) {
        if (!tile.hasCustomName())
            return;
        tile.renderOverlay(new RenderOverlayInfo(0, 0, 0, context.getFont()));
    }
}