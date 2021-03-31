package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.TRAM;

public class SignalTram extends SignalBlock {

	public SignalTram() {
		super("TramSignal", 0);
	}

	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF);
	public static final SEProperty<CAR> CARSIGNAL = SEProperty.of("signalcar", CAR.OFF);
	public static final SEProperty<PED> PEDSIGNAL = SEProperty.of("signalped", PED.OFF);
}
