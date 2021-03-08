package eu.gir.girsignals.blocks;


import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalRA extends SignalBlock {

	public SignalRA() {
		super("rasignal", 2);
	}
	
	public static final SEProperty<Boolean> RA10 = SEProperty.of("ra10", false);
	public static final SEProperty<RA> RATYPE = SEProperty.of("ratype", RA.RA11A, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> RA12 = SEProperty.of("ra12", false);

	@Override
	public boolean hasCostumColor() {
		return false;
	}
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
