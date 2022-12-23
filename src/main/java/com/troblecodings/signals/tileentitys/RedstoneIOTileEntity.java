package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.Iterator;

import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.ILevelNameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneIOTileEntity extends SyncableTileEntity
        implements ILevelNameable, IChunkloadable, ISyncable, Iterable<BlockPos> {

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    private String name = null;
    private final ArrayList<BlockPos> linkedPositions = new ArrayList<>();

    @Override
    public String getName() {
        if (hasCustomName())
            return name;
        return this.getBlockType().getLocalizedName();
    }

    @Override
    public CompoundTag writeToNBT(final CompoundTag compound) {
        final ListTag list = new ListTag();
        linkedPositions.forEach(pos -> list.add(NBTUtil.createPosTag(pos)));
        compound.put(LINKED_LIST, list);
        if (this.name != null)
            compound.setString(NAME_NBT, this.name);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final CompoundTag compound) {
        super.readFromNBT(compound);
        linkedPositions.clear();
        final ListTag list = (ListTag) compound.get(LINKED_LIST);
        if (list == null)
            return;
        list.forEach(nbt -> linkedPositions.add(NBTUtil.getPosFromTag((CompoundTag) nbt)));
        if (compound.hasKey(NAME_NBT))
            this.name = compound.getString(NAME_NBT);
    }

    @Override
    public boolean hasCustomName() {
        return this.name != null;
    }

    public void link(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
        this.syncClient();
    }

    public void unlink(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
        this.syncClient();
    }

    public void sendToAll() {
        if (level.isClientSide)
            return;
        final boolean power = this.level.getBlockState(pos).getValue(RedstoneIO.POWER);
        this.linkedPositions.forEach(position -> loadChunkAndGetTile(SignalBoxTileEntity.class,
                level, position, (tile, _u) -> tile.updateRedstonInput(this.pos, power)));
    }

    @Override
    public boolean shouldRefresh(final Level world, final BlockPos pos, final BlockState oldState,
            final BlockState newSate) {
        return false;
    }

    @Override
    public void updateTag(final CompoundTag compound) {
        if (compound.hasKey(NAME_NBT)) {
            this.name = compound.getString(NAME_NBT);
            this.syncClient();
        }
    }

    @Override
    public CompoundTag getTag() {
        return writeToNBT(new CompoundTag());
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return this.linkedPositions.iterator();
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }
}
