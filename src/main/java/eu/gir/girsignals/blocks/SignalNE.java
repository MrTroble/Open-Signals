package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.NE;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalNE extends SignalBlock {
	
	public SignalNE() {
		super("ne", 1);
	}
	
	public static final SEProperty<NE> NETYPE = SEProperty.of("netype", NE.NE1, ChangeableStage.GUISTAGE);
	
	@Override
	public boolean canBeLinked() {
		return false;
	}

}
