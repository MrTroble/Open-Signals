package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.MAST_SIGN;
import net.gir.girsignals.EnumSignals.ZS2;
import net.gir.girsignals.EnumSignals.ZS3;
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
	public static final PropertyAdapter<Boolean> NE1 = new PropertyAdapter<Boolean>(PropertyBool.create("ne1"));
	public static final PropertyAdapter<Boolean> ZS1 = new PropertyAdapter<Boolean>(PropertyBool.create("zs1"));
	public static final PropertyAdapter<ZS2> ZS2 = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2", ZS2.class));
	public static final PropertyAdapter<ZS2> ZS2V = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2v", ZS2.class));
	public static final PropertyAdapter<ZS3> ZS3 = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3", ZS3.class));
	public static final PropertyAdapter<ZS3> ZS3V = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3v", ZS3.class));
	public static final PropertyAdapter<Boolean> ZS6 = new PropertyAdapter<Boolean>(PropertyBool.create("zs6"));
	public static final PropertyAdapter<Boolean> ZS7 = new PropertyAdapter<Boolean>(PropertyBool.create("zs7"));
	public static final PropertyAdapter<Boolean> ZS8 = new PropertyAdapter<Boolean>(PropertyBool.create("zs8"));
	public static final PropertyAdapter<Boolean> ZS13 = new PropertyAdapter<Boolean>(PropertyBool.create("zs13"));
}
