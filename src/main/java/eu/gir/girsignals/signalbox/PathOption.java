package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.FREE_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.USED_COLOR;

import eu.gir.girsignals.enums.LinkType;
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

    public PathOption(final EnumPathUsage pathUsage) {
        this.pathUsage = pathUsage;
    }

    public PathOption(final NBTTagCompound compound) {
        this.pathUsage = EnumPathUsage.valueOf(compound.getString(PATH_USAGE));
        for (final LinkType type : LinkType.values()) {
            if (compound.hasKey(type.name())) {
                final NBTTagCompound item = compound.getCompoundTag(type.name());
                linkedPositions[type.ordinal()] = NBTUtil.getPosFromTag(item);
            }
        }
        if (compound.hasKey(SPEED))
            this.speed = compound.getInteger(SPEED);
    }

    public NBTTagCompound writeNBT() {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setString(PATH_USAGE, this.pathUsage.name());
        for (final LinkType type : LinkType.values()) {
            final BlockPos position = linkedPositions[type.ordinal()];
            if (position != null)
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

    public void setPathUsage(final EnumPathUsage pathUsage) {
        this.pathUsage = pathUsage;
    }

    public BlockPos getLinkedPosition(final LinkType type) {
        return linkedPositions[type.ordinal()];
    }

    public void setLinkedPosition(final LinkType type, final BlockPos linkedPosition) {
        this.linkedPositions[type.ordinal()] = linkedPosition;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(final int speed) {
        this.speed = speed;
    }

    public static enum EnumPathUsage {

        FREE(FREE_COLOR), SELECTED(SELECTED_COLOR), USED(USED_COLOR);

        private final int color;

        private EnumPathUsage(final int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }

    }

}
