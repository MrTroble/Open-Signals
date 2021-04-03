package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;

public class SignalTram extends Signal {

	public SignalTram() {
		super("TramSignal", 0);
	}

	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF);
	public static final SEProperty<TRAM> CARSIGNAL = SEProperty.of("signalcar", TRAM.OFF);
}
