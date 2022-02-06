package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.lwjgl.util.Point;

import com.google.common.collect.Maps;

import eu.gir.girsignals.blocks.IChunkloadable;
import eu.gir.girsignals.guis.guilib.ISyncable;
import eu.gir.girsignals.linkableApi.ILinkableTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, IChunkloadable<SignalTileEnity>, ILinkableTile, Iterable<BlockPos> {
	
	private static final String LINKED_POS_LIST = "linkedPos";
	private static final String GUI_TAG = "guiTag";
	
	private ArrayList<BlockPos> linkedBlocks = new ArrayList<>();
	private NBTTagCompound guiTag = new NBTTagCompound();
	private HashMap<Point, SignalNode> modeGrid = new HashMap<>(100);
	private ArrayList<ArrayList<SignalNode>> ways = new ArrayList<ArrayList<SignalNode>>();
	private boolean updated = false;
	
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
	
	public static enum EnumGUIMode {
		STRAIGHT,
		CORNER,
		END,
		PLATFORM,
		BUE,
		HP,
		VP,
		RS,
		RA10;
	}
	
	private void updateGrid() {
		updated = false;
		modeGrid.clear();
		this.guiTag.getKeySet().forEach(key -> {
			final String[] names = key.split("\\.");
			final int x = Integer.parseInt(names[0]);
			final int y = Integer.parseInt(names[1]);
			final NBTTagCompound tag = (NBTTagCompound) this.guiTag.getTag(key);
			final SignalNode node = new SignalNode(new Point(x, y));
			tag.getKeySet().forEach(modes -> {
				final String[] mNames = modes.split("\\.");
				final EnumGUIMode mode = EnumGUIMode.valueOf(mNames[0]);
				final Rotation rotate = Rotation.valueOf(mNames[1]);
				node.add(mode, rotate);
			});
			node.post();
			modeGrid.put(node.getPoint(), node);
		});
		updated = true;
	}
	
	private double calculateHeuristic(Point p1, Point p2) {
		final int dX = p2.getX() - p1.getX();
		final int dY = p2.getY() - p1.getY();
		return Math.hypot(dX, dY);
	}
	
	@SuppressWarnings("unchecked")
	private void requestWay(final Point p1, final Point p2) {
		if(!this.modeGrid.containsKey(p1) || !this.modeGrid.containsKey(p2))
			return;
		final HashMap<Point, Point> closedList = new HashMap<>();
		final Set<Point> openList = new HashSet<Point>();
		final HashMap<Point, Double> fscores = new HashMap<>();
		final HashMap<Point, Double> gscores = new HashMap<>();

		openList.add(p1);
		gscores.put(p1, 0.0);
		fscores.put(p1, calculateHeuristic(p1, p2));
		while (!openList.isEmpty()) {
			final Point cnode = openList.stream().min((n1, n2) -> {
				return Double.compare(fscores.getOrDefault(n1, Double.MAX_VALUE), fscores.getOrDefault(n2, Double.MAX_VALUE));
			}).get();
			if(cnode.equals(p2)) {
				final ArrayList<SignalNode> nodes = new ArrayList<>();
				for(Point p = cnode; p != null; p = closedList.get(p)) {
					nodes.add(this.modeGrid.get(p));
				}
				this.ways.add(nodes);
				return;
			}
			openList.remove(cnode);
			final SignalNode cSNode = this.modeGrid.get(cnode);
			if(cSNode == null)
				continue;
			for(Entry<Point, Point> e : cSNode.connections()) {
				for(Entry<Point, Point> pE : new Entry[] {e, Maps.immutableEntry(e.getValue(), e.getKey())}) {
					if(pE.getKey() == null || pE.getValue() == null)
						continue;
					final Point neighbor = pE.getValue();
					if(closedList.containsKey(pE.getKey()) || p1.equals(pE.getKey()) || closedList.isEmpty()) {
						final double tScore = gscores.getOrDefault(cnode, Double.MAX_VALUE - 1) + 1;
						if(tScore < gscores.getOrDefault(neighbor, Double.MAX_VALUE)) {
							closedList.put(neighbor, cnode);
							gscores.put(neighbor, tScore);
							fscores.put(neighbor, tScore + calculateHeuristic(neighbor, p2));
							if(!openList.contains(neighbor)) {
								openList.add(neighbor);
							}
						}
					}
				}
			}
		}
		return;
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		if (compound == null)
			return;
		if (compound.hasKey("requestWay")) {
			final NBTTagCompound comp = (NBTTagCompound) compound.getTag("requestWay");
			final Point p1 = new Point(comp.getInteger("xP1"), comp.getInteger("yP1"));
			final Point p2 = new Point(comp.getInteger("xP2"), comp.getInteger("yP2"));
			requestWay(p1, p2);
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
	
	public Collection<SignalNode> nodes() {
		return this.modeGrid.values();
	}
	
	public boolean isUpdated() {
		return updated;
	}
	
	public ArrayList<ArrayList<SignalNode>> getWays() {
		return ways;
	}
	
}
