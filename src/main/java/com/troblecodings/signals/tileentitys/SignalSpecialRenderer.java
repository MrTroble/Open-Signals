package com.troblecodings.signals.tileentitys;

import com.troblecodings.signals.core.RenderOverlayInfo;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEntity> {

    @Override
    public void render(final SignalTileEntity te, final double x, final double y, final double z,
            final float partialTicks, final int destroyStage, final float alpha) {
        if (!te.hasCustomName())
            return;
        te.renderOverlay(new RenderOverlayInfo(x, y, z, getFontRenderer()));
    }

    @Override
    public boolean isGlobalRenderer(final SignalTileEntity te) {
        return te.hasCustomName();
    }
}