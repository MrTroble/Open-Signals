package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumSignals.KS;
import net.gir.girsignals.EnumSignals.MAST_SIGN;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.SEProperty.ChangeableStage;
import net.gir.girsignals.SEProperty;

public class SignalKS extends SignalBlock {

	public SignalKS() {
		super("KS");
	}

	public static final SEProperty<KS> STOPSIGNAL = SEProperty.of("kombisignal", KS.OFF);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant", false);
	public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
}
