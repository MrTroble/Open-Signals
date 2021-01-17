package net.gir.girsignals.tileentitys;

import net.gir.girsignals.blocks.SignalBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;

public class SignalSpecialRenderer extends TileEntitySpecialRenderer<SignalTileEnity> {

	@Override
	public void render(SignalTileEnity te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		IBlockState state = te.getWorld().getBlockState(te.getPos());
		EnumFacing face = state.getValue(SignalBlock.FACING);
		float angel = face.getHorizontalAngle();
		String display = "TEST";
		FontRenderer font = getFontRenderer();
		int width = font.getStringWidth(display);
		Vec3i vec = face.getDirectionVec();
		GlStateManager.translate(x + 0.5, y + 1, z + 0.5 + vec.getX()*0.5);
		GlStateManager.rotate(angel, 0, 1, 0);
		GlStateManager.translate(-width * 0.01f, 0, 0);
		GlStateManager.scale(0.02f * -(1 - face.getAxis().ordinal()), -0.02f, 0.02f);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.pushMatrix();
		font.drawString(display, 0, 0, 0);
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return true;
	}
}
