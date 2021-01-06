package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumsKS.KS;
import net.gir.girsignals.EnumsKS.ZS2;
import net.gir.girsignals.EnumsKS.ZS3;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class SignalKS extends SignalBlock {

	public SignalKS() {
		super("KS");
	}
	public static final PropertyAdapter<KS> STOPSIGNAL = new PropertyAdapter<KS>(
			PropertyEnum.create("stopsignal", KS.class));
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
