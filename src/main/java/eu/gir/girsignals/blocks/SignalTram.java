package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.TRAM;

public class SignalTram extends SignalBlock {

	public SignalTram() {
		super("TramSignal", 0);
	}

	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF);
	public static final SEProperty<TRAM> CARSIGNAL = SEProperty.of("signalcar", TRAM.OFF);
}
