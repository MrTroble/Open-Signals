package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.*;

import net.minecraft.nbt.NBTTagCompound;

public class PathOption {
	
	private static final String PATH_USAGE = "pathUsage";
	
	private EnumPathUsage pathUsage;
	
	public PathOption() {
		this.pathUsage = EnumPathUsage.NONE;
	}
	
	public PathOption(EnumPathUsage pathUsage) {
		this.pathUsage = pathUsage;
	}
	
	public PathOption(NBTTagCompound compound) {
		this.pathUsage = EnumPathUsage.valueOf(compound.getString(PATH_USAGE));
	}
	
	public NBTTagCompound writeNBT() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString(PATH_USAGE, this.pathUsage.name());
		return compound;
	}
	
	public EnumPathUsage getPathUsage() {
		return pathUsage;
	}
	
	public void setPathUsage(EnumPathUsage pathUsage) {
		this.pathUsage = pathUsage;
	}
	
	public static enum EnumPathUsage {
		
		FREE(FREE_COLOR),
		SELECTED(SELECTED_COLOR),
		USED(USED_COLOR),
		NONE(FREE_COLOR);
		
		private final int color;
		
		private EnumPathUsage(int color) {
			this.color = color;
		}
		
		public int getColor() {
			return color;
		}
		
	}
	
}
