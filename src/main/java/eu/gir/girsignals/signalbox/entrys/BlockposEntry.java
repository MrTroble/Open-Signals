package eu.gir.girsignals.signalbox.entrys;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public final class BlockposEntry extends IPathEntry<BlockPos> {

    private BlockPos position = BlockPos.ORIGIN;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTTagCompound tag) {
        tag.setTag(this.getName(), NBTUtil.createPosTag(position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTTagCompound tag) {
        this.position = NBTUtil.getPosFromTag((NBTTagCompound) tag.getTag(getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BlockPos getValue() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final BlockPos pPosition) {
        this.position = pPosition;
        this.isDirty = true;
    }

}
