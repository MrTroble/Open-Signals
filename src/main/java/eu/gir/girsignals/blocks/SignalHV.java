package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalHV extends SignalBlock {

	public SignalHV() {
		super("HV", 6, 2.775f);
	}

	public static final SEProperty<HP> STOPSIGNAL = SEProperty.of("stopsignal", HP.OFF);
	public static final SEProperty<VR> DISTANTSIGNAL = SEProperty.of("distantsignal", VR.OFF);
	public static final SEProperty<Boolean> VR_LIGHT = SEProperty.of("vrlight", false);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF,
			ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
	public static final SEProperty<Boolean> ZS1 = SEProperty.of("zs1", false);
	public static final SEProperty<Boolean> ZS7 = SEProperty.of("zs7", false);
}
