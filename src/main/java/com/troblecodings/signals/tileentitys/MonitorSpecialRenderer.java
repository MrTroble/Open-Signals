package com.troblecodings.signals.tileentitys;

import com.troblecodings.signals.core.RenderAnimationInfo;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class MonitorSpecialRenderer extends TileEntitySpecialRenderer<MonitorTileEntity> {

    @Override
    public void render(final MonitorTileEntity te, final double x, final double y, final double z,
            final float partialTicks, final int destroyStage, final float alpha) {
        te.render(new RenderAnimationInfo(x, y, z, partialTicks, partialTicks));
    }

}