package eu.gir.girsignals.blocks;


import eu.gir.girsignals.EnumSignals.OTHER_SIGAL;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalOTHER extends Signal {

	public SignalOTHER() {
		super(GIRItems.SIGN_PLACEMENT_TOOL, "othersignal", 1);
	}
	
	public static final SEProperty<OTHER_SIGAL> OTHERTYPE = SEProperty.of("othertype", OTHER_SIGAL.HM, ChangeableStage.GUISTAGE);
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
	@Override
	public float getCustomnameRenderHeight(World world, BlockPos pos, SignalTileEnity te) {
		if(te == null || te.getProperty(OTHERTYPE).filter(OTHER_SIGAL.HM::equals).isPresent())
			return 2.1f;
		return super.getCustomnameRenderHeight(world, pos, te);
	}
	
	@Override
	public float getCustomnameScale(World world, BlockPos pos, SignalTileEnity te) {
		return 3.5f;
	}
	
	@Override
	public float getCustomnameSignWidth(World world, BlockPos pos, SignalTileEnity te) {
		return super.getCustomnameSignWidth(world, pos, te);
	}
	
	@Override
	public float getCustomnameOffsetX(World world, BlockPos pos, SignalTileEnity te) {
		return -5;
	}
	
	@Override
	public float getCustomnameOffsetZ(World world, BlockPos pos, SignalTileEnity te) {
		return 1.8f;
	}
	
}
