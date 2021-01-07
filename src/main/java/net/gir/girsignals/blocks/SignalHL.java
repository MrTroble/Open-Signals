package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumSignals.HL;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.EnumSignals.MAST_SIGN;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class SignalHL extends SignalBlock {

	public SignalHL() {
		super("HL");
	}

	public static final PropertyAdapter<HL> STOPSIGNAL = new PropertyAdapter<HL>(
			PropertyEnum.create("stopsignal", HL.class));
	public static final PropertyAdapter<Boolean> SHUNTINGSIGNAL = new PropertyAdapter<Boolean>(
			PropertyBool.create("shuntingsignal"));
	public static final PropertyAdapter<Boolean> STATUS_LIGHT = new PropertyAdapter<Boolean>(
			PropertyBool.create("statuslight"));
	public static final PropertyAdapter<Boolean> NE2 = new PropertyAdapter<Boolean>(PropertyBool.create("ne2"));
	public static final PropertyAdapter<Boolean> NE2_2 = new PropertyAdapter<Boolean>(PropertyBool.create("zs22"));
	public static final PropertyAdapter<Boolean> MAST_NUMBER = new PropertyAdapter<Boolean>(PropertyBool.create("mastnumber"));
	public static final PropertyAdapter<MAST_SIGN> MAST_SIGN = new PropertyAdapter<MAST_SIGN>(PropertyEnum.create("mastsign",MAST_SIGN.class));
	public static final PropertyAdapter<Boolean> MAST_SIGN_DISTANT = new PropertyAdapter<Boolean>(PropertyBool.create("mastsigndistants"));
	public static final PropertyAdapter<Boolean> ZS1 = new PropertyAdapter<Boolean>(PropertyBool.create("zs1"));
	public static final PropertyAdapter<ZS32> ZS2 = new PropertyAdapter<ZS32>(PropertyEnum.create("zs2", ZS32.class));
	public static final PropertyAdapter<ZS32> ZS2V = new PropertyAdapter<ZS32>(PropertyEnum.create("zs2v", ZS32.class));
	public static final PropertyAdapter<Boolean> ZS6 = new PropertyAdapter<Boolean>(PropertyBool.create("zs6"));
	public static final PropertyAdapter<Boolean> ZS8 = new PropertyAdapter<Boolean>(PropertyBool.create("zs8"));
	public static final PropertyAdapter<Boolean> ZS13 = new PropertyAdapter<Boolean>(PropertyBool.create("zs13"));
}
