package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;

public class RedstoneIOTileEntity extends TileEntity implements IWorldNameable, IChunkloadable {
	
	public static final String STATUS = "status";
	
	private static final String LINKED_LIST = "linkedList";
	
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
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		linkedPositions.clear();
		final NBTTagList list = (NBTTagList) compound.getTag(LINKED_LIST);
		list.forEach(nbt -> linkedPositions.add(NBTUtil.getPosFromTag((NBTTagCompound) nbt)));
	}
	
	@Override
	public boolean hasCustomName() {
		return this.name != null;
	}
	
	public void link(BlockPos pos) {
		if(world.isRemote)
			return;
		if (!linkedPositions.contains(pos))
			linkedPositions.add(pos);
	}
	
	public void unlink(BlockPos pos) {
		if(world.isRemote)
			return;
		if (linkedPositions.contains(pos))
			linkedPositions.remove(pos);
	}
	
	public void sendToAll() {
		if(world.isRemote)
			return;
		final boolean power = this.world.getBlockState(pos).getValue(RedstoneIO.POWER);
		this.linkedPositions.forEach(position -> loadChunkAndGetTile(SignalBoxTileEntity.class, world, position, (tile, _u) -> tile.updateRedstonInput(position, power)));
	}
}
