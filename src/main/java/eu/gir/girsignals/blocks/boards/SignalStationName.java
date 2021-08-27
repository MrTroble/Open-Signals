package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SignalStationName extends Signal {

	public SignalStationName() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "signname").height(0).signHeight(0.5f).offsetY(2f).noLink().build());
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side) {
		return true;
	}

	private static final float WIDTH_NORM = 56.0f;
	
	@Override
	public void renderOverlay(double x, double y, double z, SignalTileEnity te, FontRenderer font,
			final float renderHeight) {
		final World world = te.getWorld();
		final BlockPos pos = te.getPos();
		final IBlockState state = world.getBlockState(pos);
		final SignalAngel face = state.getValue(Signal.ANGEL);
		final float angel = face.getAngel();

		final String display = te.getDisplayName().getFormattedText();
		final float width = font.getStringWidth(display);		
		final float scale = Math.min(1/ (22 * (width / WIDTH_NORM)), 0.1f);
		
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5f, y + 0.75f, z + 0.5f);
		GlStateManager.rotate(angel, 0, 1, 0);
		GlStateManager.scale(-scale, -scale, 1);
		GlStateManager.translate(-1.3f / scale, 0, -0.32f);
		font.drawString(display, 0, 0, 0xFFFFFFFF);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

}
