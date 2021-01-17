package net.gir.girsignals.tileentitys;

import net.gir.girsignals.blocks.SignalBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {

	private static int MAX_WIDTH = 20;
	
	@Override
	public void render(SignalTileEnity te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		if(!te.hasCustomName()) return;
		IBlockState state = te.getWorld().getBlockState(te.getPos());
		EnumFacing face = state.getValue(SignalBlock.FACING);
		float angel = face.getHorizontalAngle();
		String display = te.getDisplayName().getFormattedText();
		FontRenderer font = getFontRenderer();
		Vec3i vec = face.getDirectionVec();
		
		GlStateManager.translate(x + 0.5, y + te.getCustomNameRenderHeight(), z + 0.5 + vec.getX()*0.5);
		GlStateManager.rotate(angel, 0, 1, 0);
		GlStateManager.translate(MAX_WIDTH * -0.0075f, 0, 0.07f);
		GlStateManager.scale(0.015f * -(1 - face.getAxis().ordinal()), -0.015f, 0.015f);
		GlStateManager.pushMatrix();
		font.drawSplitString(display, 0, 0, MAX_WIDTH, 0);
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return te.hasCustomName();
	}
}
