package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalSH extends SignalBlock {

	public SignalSH() {
		super("shsignal", 1);
	}

	public static final SEProperty<Boolean> SH = SEProperty.of("sh2", true, ChangeableStage.GUISTAGE);

	@Override
	public boolean hasCostumColor() {
		return false;
	}
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
