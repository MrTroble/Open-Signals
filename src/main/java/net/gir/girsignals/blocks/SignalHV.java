package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumsHV.BinaryExtensionSignals;
import net.gir.girsignals.EnumsHV.HPVR;
import net.gir.girsignals.EnumsHV.ZS2;
import net.gir.girsignals.EnumsHV.ZS3;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class SignalHV extends SignalBlock {

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
	public static final PropertyAdapter<BinaryExtensionSignals> ZS1 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs1", BinaryExtensionSignals.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS6 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs6", BinaryExtensionSignals.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS8 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs8", BinaryExtensionSignals.class));
	public static final PropertyAdapter<ZS2> ZS2 = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2", ZS2.class));
	public static final PropertyAdapter<ZS2> ZS2V = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2v", ZS2.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS7 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs7", BinaryExtensionSignals.class));


}
