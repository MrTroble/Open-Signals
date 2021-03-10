package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.EL;
import eu.gir.girsignals.EnumSignals.EL_ARROW;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalEL extends SignalBlock {

	public SignalEL() {
		super("elsignal", 2);
	}

	public static final SEProperty<EL> ELTYPE = SEProperty.of("eltype", EL.EL1V, ChangeableStage.GUISTAGE);
	public static final SEProperty<EL_ARROW> ELARROW = SEProperty.of("elarrow", EL_ARROW.OFF, ChangeableStage.GUISTAGE);

	@Override
	public boolean hasCostumColor() {
		return false;
	}
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
