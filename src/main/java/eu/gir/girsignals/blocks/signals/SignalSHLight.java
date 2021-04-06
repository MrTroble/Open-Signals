package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.SH_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalSHLight extends Signal {

	public SignalSHLight() {
		super(GIRItems.PLACEMENT_TOOL, "SHLight", 0);
	}

	public static final SEProperty<SH_LIGHT> SHLIGHT_0 = SEProperty.of("sh_light_bottom", SH_LIGHT.OFF);
	public static final SEProperty<SH_LIGHT> SHLIGHT_2 = SEProperty.of("sh_light_top", SH_LIGHT.OFF);

	@Override
	public int getHeight(NBTTagCompound comp) {
		if (comp.getBoolean(SHLIGHT_2.getName())) {
			return 2;
		}
		return super.getHeight(comp);
	}
	
	@Override
	public float getSignWidth(World world, BlockPos pos, SignalTileEnity te) {
		// TODO Auto-generated method stub
		return super.getSignWidth(world, pos, te);
	}
	
	@Override
	public float getOffsetX(World world, BlockPos pos, SignalTileEnity te) {
		return -12.5f;
	}
	
	@Override
	public float getOffsetZ(World world, BlockPos pos, SignalTileEnity te) {
		return -6.3f;
	}
	
	@Override
	public float getCustomnameRenderHeight(World world, BlockPos pos, SignalTileEnity te) {
		if(te != null && te.getProperty(SHLIGHT_2).isPresent())
			return 2.35f;
		return 0.35f;
	}
	
}
