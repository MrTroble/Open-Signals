package eu.gir.girsignals.blocks;


import eu.gir.girsignals.EnumSignals.OTHER_SIGAL;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalOTHER extends SignalBlock {

	public SignalOTHER() {
		super("othersignal", 1);
	}
	
	public static final SEProperty<OTHER_SIGAL> OTHERTYPE = SEProperty.of("othertype", OTHER_SIGAL.HM, ChangeableStage.GUISTAGE);
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
