package eu.gir.girsignals.tileentitys;

import eu.gir.girsignals.blocks.SignalBlock;
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
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + te.getCustomNameRenderHeight(), z + 0.5f + vec.getX()*0.5);
		int ordinalFactor = -(1 - face.getAxis().ordinal());
		int ordinal = face.getAxis().ordinal() / 2;
		int nordinal = 1 - ordinal;
		GlStateManager.rotate(angel, 0, 1, 0);
		GlStateManager.translate(MAX_WIDTH*0.0075f*-ordinalFactor - nordinal*0.5, 0, ordinalFactor * 0.07f);
		GlStateManager.scale(0.015f * ordinalFactor, -0.015f, 0.015f);
		font.drawSplitString(display, 0, 0, MAX_WIDTH, 0);
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean isGlobalRenderer(SignalTileEnity te) {
		return te.hasCustomName();
	}
}
