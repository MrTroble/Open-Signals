package eu.gir.girsignals.tileentitys;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {

    @Override
    public void render(SignalTileEnity te, double x, double y, double z, float partialTicks,
            int destroyStage, float alpha) {
        if (!te.hasCustomName())
            return;
        te.renderOverlay(x, y, z, getFontRenderer());
    }

    @Override
    public boolean isGlobalRenderer(SignalTileEnity te) {
        return te.hasCustomName();
    }
}
