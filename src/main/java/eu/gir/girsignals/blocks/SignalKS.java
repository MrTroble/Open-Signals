package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.MAST_SIGN;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty.ChangeableStage;

public class SignalKS extends SignalBlock {

	public SignalKS() {
		super("KS");
	}

	public static final SEProperty<KS> STOPSIGNAL = SEProperty.of("kombisignal", KS.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
}
