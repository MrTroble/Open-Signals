package eu.gir.girsignals.blocks;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.EnumSignals.SH_LIGHT;

public class SignalSHLight extends SignalBlock {

	public SignalSHLight() {
		super("SHLight");
	}
	
	public static final SEProperty<SH_LIGHT> SHLIGHT_0 = SEProperty.of("sh_light_bottom", SH_LIGHT.OFF);
	public static final SEProperty<SH_LIGHT> SHLIGHT_2 = SEProperty.of("sh_light_top", SH_LIGHT.OFF);
}
