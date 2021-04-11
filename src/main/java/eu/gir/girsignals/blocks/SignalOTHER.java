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
		if(te.getProperty(OTHERTYPE).filter(OTHER_SIGAL.HM::equals).isPresent())
			return 1.0f;
		return super.getCustomnameRenderHeight(world, pos, te);
	}
	
	@Override
	public float getCustomnameScale(World world, BlockPos pos, SignalTileEnity te) {
		return 1.5f;
	}
}
