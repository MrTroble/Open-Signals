package eu.gir.girsignals.blocks;


import eu.gir.girsignals.EnumSignals.RA;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import net.minecraft.nbt.NBTTagCompound;

public class SignalRA extends SignalBlock {

	public SignalRA() {
		super("rasignal", 3);
	}
	
	public static final SEProperty<RA> RATYPE = SEProperty.of("ratype", RA.RA10, ChangeableStage.GUISTAGE);
	
	@Override
	public boolean canBeLinked() {
		return false;
	}
	
}
