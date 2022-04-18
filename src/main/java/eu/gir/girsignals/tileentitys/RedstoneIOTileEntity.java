package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;
import java.util.Iterator;

import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;

public class RedstoneIOTileEntity extends SyncableTileEntity
        implements IWorldNameable, IChunkloadable, ISyncable, Iterable<BlockPos> {

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    private String name = null;
    private ArrayList<BlockPos> linkedPositions = new ArrayList<>();

    @Override
    public String getName() {
        if (hasCustomName())
            return name;
        return this.getBlockType().getLocalizedName();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        final NBTTagList list = new NBTTagList();
        linkedPositions.forEach(pos -> list.appendTag(NBTUtil.createPosTag(pos)));
        compound.setTag(LINKED_LIST, list);
        if (this.name != null)
            compound.setString(NAME_NBT, this.name);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        linkedPositions.clear();
        final NBTTagList list = (NBTTagList) compound.getTag(LINKED_LIST);
        list.forEach(nbt -> linkedPositions.add(NBTUtil.getPosFromTag((NBTTagCompound) nbt)));
        if (compound.hasKey(NAME_NBT))
            this.name = compound.getString(NAME_NBT);
    }

    @Override
    public boolean hasCustomName() {
        return this.name != null;
    }

    public void link(BlockPos pos) {
        if (world.isRemote)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
        this.syncClient();
    }

    public void unlink(BlockPos pos) {
        if (world.isRemote)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
        this.syncClient();
    }

    public void sendToAll() {
        if (world.isRemote)
            return;
        final boolean power = this.world.getBlockState(pos).getValue(RedstoneIO.POWER);
        this.linkedPositions.forEach(position -> loadChunkAndGetTile(SignalBoxTileEntity.class,
                world, position, (tile, _u) -> tile.updateRedstonInput(this.pos, power)));
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState,
            IBlockState newSate) {
        return false;
    }

    @Override
    public void updateTag(NBTTagCompound compound) {
        if (compound.hasKey(NAME_NBT)) {
            this.name = compound.getString(NAME_NBT);
            this.syncClient();
        }
    }

    @Override
    public NBTTagCompound getTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return this.linkedPositions.iterator();
    }
}
