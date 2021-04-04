package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalTram extends Signal {

	public SignalTram() {
		super(GIRItems.PLACEMENT_TOOL, "TramSignal", 0);
	}

	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF);
	public static final SEProperty<TRAM> CARSIGNAL = SEProperty.of("signalcar", TRAM.OFF);
}
