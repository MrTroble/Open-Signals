package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.init.GIRItems;

public class SignalSH extends Signal {

	public SignalSH() {
		super(GIRItems.SIGN_PLACEMENT_TOOL, "shsignal", 1);
	}

	public static final SEProperty<Boolean> SH = SEProperty.of("sh2", false);

	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
