package com.troblecodings.signals.tileentitys;

import java.util.ArrayList;
import java.util.Iterator;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class RedstoneIOTileEntity extends SyncableTileEntity
		implements NamableWrapper, IChunkloadable, ISyncable, Iterable<BlockPos> {
	
	public RedstoneIOTileEntity(TileEntityInfo info) {
		super(info);
	}

	public static final String NAME_NBT = "name";
	public static final String LINKED_LIST = "linkedList";

	private String name = null;
	private final ArrayList<BlockPos> linkedPositions = new ArrayList<>();

	@Override
	public String getNameWrapper() {
		if (hasCustomName())
			return name;
		return this.getBlockState().getBlock().getDescriptionId();
	}

	@Override
	public void saveWrapper(NBTWrapper wrapper) {
		wrapper.putList(LINKED_LIST, linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper).toList());
		if (this.name != null)
			wrapper.putString(NAME_NBT, this.name);
	}
	
	@Override
	public void loadWrapper(NBTWrapper wrapper) {
		linkedPositions.clear();
		wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos).forEach(linkedPositions::add);;
		if (wrapper.contains(NAME_NBT))
			this.name = wrapper.getString(NAME_NBT);
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
		final boolean power = this.level.getBlockState(this.worldPosition).getValue(RedstoneIO.POWER);
		this.linkedPositions.forEach(position -> loadChunkAndGetTile(SignalBoxTileEntity.class, level, position,
				(tile, _u) -> tile.updateRedstonInput(this.worldPosition, power)));
	}

	
	@Override
	public void updateTag(final CompoundTag compound) {
		final NBTWrapper wrapper = new NBTWrapper(compound);
		if (wrapper.contains(NAME_NBT)) {
			this.name = wrapper.getString(NAME_NBT);
			this.syncClient();
		}
	}

	@Override
	public Iterator<BlockPos> iterator() {
		return this.linkedPositions.iterator();
	}

	@Override
	public boolean isValid(final Player player) {
		return true;
	}

	@Override
	public CompoundTag getTag() {
		return null;
	}
}
