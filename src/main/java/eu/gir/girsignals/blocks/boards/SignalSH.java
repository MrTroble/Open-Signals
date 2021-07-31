package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalSH extends Signal {

	public SignalSH() {
		super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "shsignal").noLink().build());
	}

}
