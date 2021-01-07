package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.MAST_SIGN;
import net.gir.girsignals.EnumSignals.ZS32;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class SignalHV extends SignalBlock {

	public SignalHV() {
		super("HV");
	}

	public static final PropertyAdapter<HPVR> STOPSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("stopsignal", HPVR.class));
	public static final PropertyAdapter<HPVR> DISTANTSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("distantsignal", HPVR.class));
	public static final PropertyAdapter<Boolean> SHUNTINGSIGNAL = new PropertyAdapter<Boolean>(
			PropertyBool.create("shuntingsignal"));
	public static final PropertyAdapter<Boolean> VR_LIGHT = new PropertyAdapter<Boolean>(
			PropertyBool.create("vrlight"));
	public static final PropertyAdapter<Boolean> STATUS_LIGHT = new PropertyAdapter<Boolean>(
			PropertyBool.create("statuslight"));
	public static final PropertyAdapter<Boolean> DISTANTS_STATUS_LIGHT = new PropertyAdapter<Boolean>(
			PropertyBool.create("distantsstatuslight"));
	public static final PropertyAdapter<Boolean> MAST_NUMBER = new PropertyAdapter<Boolean>(
			PropertyBool.create("mastnumber"));
	public static final PropertyAdapter<MAST_SIGN> MAST_SIGN = new PropertyAdapter<MAST_SIGN>(
			PropertyEnum.create("mastsign", MAST_SIGN.class));
	public static final PropertyAdapter<ZS32> ZS32 = new PropertyAdapter<ZS32>(PropertyEnum.create("zs3", ZS32.class));
	public static final PropertyAdapter<ZS32> ZS32V = new PropertyAdapter<ZS32>(PropertyEnum.create("zs3v", ZS32.class));
	public static final PropertyAdapter<Boolean> ZS1 = new PropertyAdapter<Boolean>(PropertyBool.create("zs1"));
	public static final PropertyAdapter<Boolean> ZS7 = new PropertyAdapter<Boolean>(PropertyBool.create("zs7"));
}
