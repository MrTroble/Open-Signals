package eu.gir.girsignals.tileentitys;

import eu.gir.girsignals.blocks.SignalBlock;
import eu.gir.girsignals.blocks.SignalBlock.SignalAngel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {
	
	@Override
	public void render(SignalTileEnity te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		if(!te.hasCustomName()) return;
		final IBlockState state = te.getWorld().getBlockState(te.getPos());
 		final SignalAngel face = state.getValue(SignalBlock.ANGEL);
 		final float angel = face.getAngel();
 		final String display = te.getDisplayName().getFormattedText();
 		final FontRenderer font = getFontRenderer();
 		final float width = te.getSignWidth();
 		final float offsetX = te.getOffset(); 
 		
 		GlStateManager.enableAlpha();
 		GlStateManager.pushMatrix();
 		GlStateManager.translate(x + 0.5f, y + te.getCustomNameRenderHeight(), z + 0.5f);
 		GlStateManager.scale(0.015f, -0.015f, 0.015f);
 		GlStateManager.rotate(angel, 0, 1, 0);
 		GlStateManager.translate(width/2 + offsetX, 0, -4.2f);
 		GlStateManager.scale(-1f, 1f, 1f);
 		font.drawSplitString(display, 0, 0, (int) width, 0);
 		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return te.hasCustomName();
	}
}
