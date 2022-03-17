package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.FREE_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.USED_COLOR;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class PathOption {
	
	private static final String PATH_USAGE = "pathUsage";
	private static final String SPEED = "speed";
	
	private EnumPathUsage pathUsage;
	private final BlockPos[] linkedPositions = new BlockPos[LinkType.values().length];
	private int speed = Integer.MAX_VALUE;
	
	public PathOption() {
		this.pathUsage = EnumPathUsage.FREE;
	}
	
	public PathOption(EnumPathUsage pathUsage) {
		this.pathUsage = pathUsage;
	}
	
	public PathOption(NBTTagCompound compound) {
		this.pathUsage = EnumPathUsage.valueOf(compound.getString(PATH_USAGE));
		for (final LinkType type : LinkType.values()) {
			final NBTTagCompound item = compound.getCompoundTag(type.name());
			linkedPositions[type.ordinal()] = NBTUtil.getPosFromTag(item);
		}
		if (compound.hasKey(SPEED))
			this.speed = compound.getInteger(SPEED);
	}
	
	public NBTTagCompound writeNBT() {
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString(PATH_USAGE, this.pathUsage.name());
		for (final LinkType type : LinkType.values()) {
			final BlockPos position = linkedPositions[type.ordinal()];
			if(position != null)
				compound.setTag(type.name(), NBTUtil.createPosTag(position));
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
	
	public BlockPos getLinkedPosition(LinkType type) {
		return linkedPositions[type.ordinal()];
	}
	
	public void setLinkedPosition(LinkType type, BlockPos linkedPosition) {
		this.linkedPositions[type.ordinal()] = linkedPosition;
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
