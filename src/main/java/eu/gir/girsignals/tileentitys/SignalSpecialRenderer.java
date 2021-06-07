package eu.gir.girsignals.tileentitys;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {
	
	@Override
	public void render(SignalTileEnity te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		final float height = te.getCustomNameRenderHeight();
		if(!te.hasCustomName() || height == 0) return;
		final IBlockState state = te.getWorld().getBlockState(te.getPos());
 		final SignalAngel face = state.getValue(Signal.ANGEL);
 		final float angel = face.getAngel();
 		final String[] display = te.getDisplayName().getUnformattedComponentText().split("\\[n\\]");
 		final FontRenderer font = getFontRenderer();
 		final float width = te.getCustomnameSignWidth();
 		final float offsetX = te.getCustomnameOffsetX(); 
 		final float offsetZ = te.getCustomnameOffsetZ(); 
 		final float scale = te.getCustomnameScale(); 

 		GlStateManager.enableAlpha();
 		GlStateManager.pushMatrix();
 		GlStateManager.translate(x + 0.5f, y + height, z + 0.5f);
 		GlStateManager.scale(0.015f * scale, -0.015f * scale, 0.015f * scale);
 		GlStateManager.rotate(angel, 0, 1, 0);
 		GlStateManager.translate(width/2 + offsetX, 0, -4.2f + offsetZ);
 		GlStateManager.scale(-1f, 1f, 1f);
 		for (int i = 0; i < display.length; i++) {
			 font.drawSplitString(display[i], 0, (int) (i * scale * 2.8f), (int) width, 0);
 		}
 		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return te.hasCustomName();
	}
}
