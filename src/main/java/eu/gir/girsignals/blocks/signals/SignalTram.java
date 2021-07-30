package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalTram extends Signal {

	public SignalTram() {
		super(builder(GIRItems.PLACEMENT_TOOL, "TramSignal").height(0).build());
	}

	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF);
	public static final SEProperty<CAR> CARSIGNAL = SEProperty.of("signalcar", CAR.OFF);
	public static final SEProperty<PED> PEDSIGNAL = SEProperty.of("signalped", PED.OFF);
}
