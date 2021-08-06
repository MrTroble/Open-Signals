package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.CAR;
import eu.gir.girsignals.EnumSignals.PED;
import eu.gir.girsignals.EnumSignals.TRAM;
import eu.gir.girsignals.EnumSignals.TRAMTYPE;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;

public class SignalTram extends Signal {

	public SignalTram() {
		super(builder(GIRItems.PLACEMENT_TOOL, "TramSignal").height(0).build());
	}

	public static final SEProperty<TRAMTYPE> TRAMSIGNAL_TYPE = SEProperty.of("signaltramtype", TRAMTYPE.TRAM, ChangeableStage.GUISTAGE);
	public static final SEProperty<TRAM> TRAMSIGNAL = SEProperty.of("signaltram", TRAM.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, false, t -> t.entrySet().stream().anyMatch((e -> e.getKey().equals(TRAMSIGNAL_TYPE) && e.getValue().equals(TRAMTYPE.TRAM))));
	public static final SEProperty<CAR> CARSIGNAL = SEProperty.of("signalcar", CAR.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, false, t -> t.entrySet().stream().anyMatch((e -> e.getKey().equals(TRAMSIGNAL_TYPE) && e.getValue().equals(TRAMTYPE.CAR))));
	public static final SEProperty<PED> PEDSIGNAL = SEProperty.of("signalped", PED.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, false, t -> t.entrySet().stream().anyMatch((e -> e.getKey().equals(TRAMSIGNAL_TYPE) && e.getValue().equals(TRAMTYPE.PEDESTRIAN))));
}
