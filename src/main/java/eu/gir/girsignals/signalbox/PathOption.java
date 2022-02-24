package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.FREE_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.USED_COLOR;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class PathOption {
	
	private static final String PATH_USAGE = "pathUsage";
	private static final String POSITION = "position";
	private static final String SPEED = "speed";
	
	private EnumPathUsage pathUsage;
	private BlockPos linkedPosition;
	private int speed = Integer.MAX_VALUE;
	
	public PathOption() {
		this.pathUsage = EnumPathUsage.FREE;
	}
	
	public PathOption(EnumPathUsage pathUsage) {
		this.pathUsage = pathUsage;
	}
	
	public PathOption(NBTTagCompound compound) {
		this.pathUsage = EnumPathUsage.valueOf(compound.getString(PATH_USAGE));
		if (compound.hasKey(POSITION)) {
			this.linkedPosition = NBTUtil.getPosFromTag(compound.getCompoundTag(POSITION));
		} else {
			this.linkedPosition = null;
		}
		if (compound.hasKey(SPEED))
			this.speed = compound.getInteger(SPEED);
	}
	
	public NBTTagCompound writeNBT() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString(PATH_USAGE, this.pathUsage.name());
		if (linkedPosition != null) {
			compound.setTag(POSITION, NBTUtil.createPosTag(linkedPosition));
		} else {
			compound.removeTag(POSITION);
		}
		if (speed != Integer.MAX_VALUE) {
			compound.setInteger(SPEED, speed);
		} else {
			compound.removeTag(SPEED);
		}
		return compound;
	}
	
	public EnumPathUsage getPathUsage() {
		return pathUsage;
	}
	
	public void setPathUsage(EnumPathUsage pathUsage) {
		this.pathUsage = pathUsage;
	}
	
	public BlockPos getLinkedPosition() {
		return linkedPosition;
	}
	
	public void setLinkedPosition(BlockPos linkedPosition) {
		this.linkedPosition = linkedPosition;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public static enum EnumPathUsage {
		
		FREE(FREE_COLOR),
		SELECTED(SELECTED_COLOR),
		USED(USED_COLOR);
		
		private final int color;
		
		private EnumPathUsage(int color) {
			this.color = color;
		}
		
		public int getColor() {
			return color;
		}
		
	}
	
}
