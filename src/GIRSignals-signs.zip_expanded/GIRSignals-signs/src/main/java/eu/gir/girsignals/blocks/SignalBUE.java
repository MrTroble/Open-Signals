package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.BUE;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalBUE extends SignalBlock {

	public SignalBUE() {
		super("buesignal", 2);
	}

	public static final SEProperty<BUE> BUETYPE = SEProperty.of("buetype", BUE.BUE4, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> BUEADD = SEProperty.of("bueadd", false);

	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
