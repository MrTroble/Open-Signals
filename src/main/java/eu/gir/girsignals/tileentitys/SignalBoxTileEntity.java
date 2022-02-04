package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;
import java.util.Iterator;

import eu.gir.girsignals.blocks.IChunkloadable;
import eu.gir.girsignals.guis.guilib.ISyncable;
import eu.gir.girsignals.linkableApi.ILinkableTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class SignalBoxTileEntity extends TileEntity implements ISyncable, IChunkloadable<SignalTileEnity>, ILinkableTile, Iterable<BlockPos> {
	
	private static final String LINKED_POS_LIST = "linkedPos";
	
	private ArrayList<BlockPos> linkedBlocks = new ArrayList<>();
	private NBTTagCompound guiTag;
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		final NBTTagList list = new NBTTagList();
		linkedBlocks.forEach(p -> list.appendTag(NBTUtil.createPosTag(p)));
		compound.setTag(LINKED_POS_LIST, list);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		final NBTTagList list = (NBTTagList) compound.getTag(LINKED_POS_LIST);
		if (list != null) {
			linkedBlocks.clear();
			list.forEach(pos -> linkedBlocks.add(NBTUtil.getPosFromTag((NBTTagCompound) pos)));
		}
		super.readFromNBT(compound);
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		this.guiTag = compound;
	}
	
	@Override
	public NBTTagCompound getTag() {
		return this.guiTag;
	}
	
	@Override
	public boolean hasLink() {
		return !linkedBlocks.isEmpty();
	}
	
	@Override
	public boolean link(BlockPos pos) {
		if (linkedBlocks.contains(pos))
			return false;
		linkedBlocks.add(pos);
		syncClient();
		return true;
	}
	
	@Override
	public boolean unlink() {
		linkedBlocks.clear();
		syncClient();
		return true;
	}
		
	@Override
	public Iterator<BlockPos> iterator() {
		return linkedBlocks.iterator();
	}
	
}
