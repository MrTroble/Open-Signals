package eu.gir.girsignals.blocks.boards;

import java.util.HashMap;

import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.EnumSignals.RA_LIGHT;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalRA extends Signal {
	
	public SignalRA() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "rasignal").height(0).build());
	}
	
	public static final SEProperty<RA> RATYPE = SEProperty.of("ratype", RA.RA10, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> RALIGHT = SEProperty.of("ralight", false, ChangeableStage.APISTAGE, true, check(RATYPE, RA.RA11A).or(check(RATYPE, RA.RA11B)));
	public static final SEProperty<RA_LIGHT> RALIGHTSIGNAL = SEProperty.of("ralightsignal", RA_LIGHT.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true, check(RATYPE, RA.RA6_9));
	
	@Override
	public int getHeight(final HashMap<SEProperty<?>, Object> map) {
		final RA ra = (RA) map.get(RATYPE);
		if (ra == null)
			return super.getHeight(map);
		switch (ra) {
		case RA10:
			return 1;
		case RA11A:
		case RA11B:
			return 3;
		case RA6_9:
			return 4;
		default:
			return super.getHeight(map);
		}
	}
	
}
