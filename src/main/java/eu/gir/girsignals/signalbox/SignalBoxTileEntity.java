package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.requestWay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.lwjgl.util.Point;

import com.google.common.collect.Maps;

import eu.gir.girsignals.blocks.IChunkloadable;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.IIntegerable;
import eu.gir.girsignals.guis.guilib.ISyncable;
import eu.gir.girsignals.linkableApi.ILinkableTile;
import eu.gir.girsignals.signalbox.PathOption.EnumPathUsage;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.girsignals.tileentitys.SyncableTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, IChunkloadable<SignalTileEnity>, ILinkableTile, Iterable<BlockPos>, IIntegerable<BlockPos> {
	
	private static final String LINKED_POS_LIST = "linkedPos";
	private static final String GUI_TAG = "guiTag";
	
	private ArrayList<BlockPos> linkedBlocks = new ArrayList<>();
	private NBTTagCompound guiTag = new NBTTagCompound();
	private HashMap<Point, SignalNode> modeGrid = new HashMap<>(100);
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		final NBTTagList list = new NBTTagList();
		linkedBlocks.forEach(p -> list.appendTag(NBTUtil.createPosTag(p)));
		compound.setTag(LINKED_POS_LIST, list);
		compound.setTag(GUI_TAG, guiTag);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		final NBTTagList list = (NBTTagList) compound.getTag(LINKED_POS_LIST);
		if (list != null) {
			linkedBlocks.clear();
			list.forEach(pos -> linkedBlocks.add(NBTUtil.getPosFromTag((NBTTagCompound) pos)));
		}
		this.guiTag = compound.getCompoundTag(GUI_TAG);
		this.updateGrid();
		super.readFromNBT(compound);
	}
	
	private void updateGrid() {
		modeGrid.clear();
		this.guiTag.getKeySet().forEach(key -> {
			final String[] names = key.split("\\.");
			if (names.length < 2)
				return;
			final int x = Integer.parseInt(names[0]);
			final int y = Integer.parseInt(names[1]);
			final SignalNode node = new SignalNode(new Point(x, y));
			node.read(this.guiTag);
			node.post();
			modeGrid.put(node.getPoint(), node);
		});
	}
	
	public void onWayAdd(final ArrayList<SignalNode> nodes) {
		for (int i = 1; i < nodes.size() - 1; i++) {
			final Point oldPos = nodes.get(i - 1).getPoint();
			final Point newPos = nodes.get(i + 1).getPoint();
			final Entry<Point, Point> entry = Maps.immutableEntry(oldPos, newPos);
			final SignalNode current = nodes.get(i);
			current.apply(entry, option -> option.setPathUsage(EnumPathUsage.SELECTED));
		}
		final NBTTagCompound update = new NBTTagCompound();
		modeGrid.values().forEach(signal -> signal.write(update));
		this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		if (compound == null)
			return;
		if (compound.hasKey(REQUEST_WAY)) {
			final NBTTagCompound request = (NBTTagCompound) compound.getTag(REQUEST_WAY);
			final Point p1 = fromNBT(request, POINT1);
			final Point p2 = fromNBT(request, POINT2);
			requestWay(modeGrid, p1, p2).ifPresent(this::onWayAdd);
			return;
		}
		this.guiTag = compound;
		this.syncClient();
		updateGrid();
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
	
	@Override
	public BlockPos getObjFromID(int obj) {
		return this.linkedBlocks.get(obj);
	}
	
	@Override
	public int count() {
		return this.linkedBlocks.size();
	}
	
	@Override
	public String getName() {
		return "signalposition";
	}
	
	@Override
	public String getNamedObj(int obj) {
		final BlockPos pos = getObjFromID(obj);
		return getLocalizedName() + ": x=" + pos.getX() + ", y=" + pos.getY() + ", z=" + pos.getZ();
	}
	
}
