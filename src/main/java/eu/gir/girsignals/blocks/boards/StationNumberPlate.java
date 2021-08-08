package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.ST_NUMBER;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class StationNumberPlate extends Signal {
	
	public StationNumberPlate() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "stationnumberplate").noLink().height(0).build());
		
	}
	
	public static final SEProperty<ST_NUMBER> STATIONNUMBER = SEProperty.of("stationnumber", ST_NUMBER.Z1,
			ChangeableStage.GUISTAGE);

}
