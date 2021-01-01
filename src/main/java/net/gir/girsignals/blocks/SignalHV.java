package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumsHV.HPVR;
import net.gir.girsignals.EnumsHV.ZS2;
import net.gir.girsignals.EnumsHV.ZS3;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class SignalHV extends SignalBlock {

	public SignalHV() {
		super("HV");
	}
	public static final PropertyAdapter<HPVR> HAUPTSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("hauptsignale", HPVR.class));
	public static final PropertyAdapter<HPVR> VORSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("vorsignale", HPVR.class));
	public static final PropertyAdapter<Boolean> RANGIERSIGNAL = new PropertyAdapter<Boolean>(
			PropertyBool.create("rangiersignale"));
	public static final PropertyAdapter<ZS3> ZS3 = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3", ZS3.class));
	public static final PropertyAdapter<Boolean> ZS3LS = new PropertyAdapter<Boolean>(PropertyBool.create("zs3ls"));
	public static final PropertyAdapter<ZS3> ZS3V = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3v", ZS3.class));
	public static final PropertyAdapter<Boolean> ZS3VLS = new PropertyAdapter<Boolean>(PropertyBool.create("zs3vls"));
	public static final PropertyAdapter<Boolean> ZS1 = new PropertyAdapter<Boolean>(PropertyBool.create("zs1"));
	public static final PropertyAdapter<Boolean> ZS6 = new PropertyAdapter<Boolean>(PropertyBool.create("zs6"));
	public static final PropertyAdapter<Boolean> ZS8 = new PropertyAdapter<Boolean>(PropertyBool.create("zs8"));
	public static final PropertyAdapter<ZS2> ZS2 = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2", ZS2.class));
	public static final PropertyAdapter<ZS2> ZS2V = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2v", ZS2.class));
	public static final PropertyAdapter<Boolean> ZS7 = new PropertyAdapter<Boolean>(PropertyBool.create("zs7"));

}
