package eu.gir.girsignals.tileentitys;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {

    @Override
    public void render(final SignalTileEnity te, final double x, final double y, final double z,
            final float partialTicks, final int destroyStage, final float alpha) {
        if (!te.hasCustomName())
            return;
        te.renderOverlay(x, y, z, getFontRenderer());
    }

    @Override
    public boolean isGlobalRenderer(final SignalTileEnity te) {
        return te.hasCustomName();
    }
}
