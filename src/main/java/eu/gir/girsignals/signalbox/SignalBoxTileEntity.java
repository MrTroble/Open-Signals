package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.requestWay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import eu.gir.girsignals.blocks.IChunkloadable;
import eu.gir.girsignals.blocks.ISignalAutoconfig;
import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.signalbox.PathOption.EnumPathUsage;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.girsignals.tileentitys.SyncableTileEntity;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import eu.gir.linkableapi.ILinkableTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, IChunkloadable<SignalTileEnity>, ILinkableTile {
	
	public static final String ERROR_STRING = "error";
	
	private static final String LINKED_POS_LIST = "linkedPos";
	private static final String GUI_TAG = "guiTag";
	private static final String LINK_TYPE = "linkType";
	
	private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>(10);
	private final Map<Point, SignalNode> modeGrid = new HashMap<>(10);
	private final Map<BlockPos, Signal> signals = new HashMap<>(10);
	private NBTTagCompound guiTag = new NBTTagCompound();
	private final HashMap<BlockPos, Entry<BlockPos, Integer>> pathWayEnd = new HashMap<>();
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		final NBTTagList list = new NBTTagList();
		linkedBlocks.forEach((p, t) -> {
			final NBTTagCompound item = NBTUtil.createPosTag(p);
			item.setString(LINK_TYPE, t.name());
			list.appendTag(item);
		});
		compound.setTag(LINKED_POS_LIST, list);
		compound.setTag(GUI_TAG, guiTag);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		final NBTTagList list = (NBTTagList) compound.getTag(LINKED_POS_LIST);
		if (list != null) {
			linkedBlocks.clear();
			list.forEach(pos -> {
				final NBTTagCompound item = (NBTTagCompound) pos;
				if(item.hasKey(LINK_TYPE))
					linkedBlocks.put(NBTUtil.getPosFromTag(item), LinkType.valueOf(item.getString(LINK_TYPE)));
			});
		}
		this.guiTag = compound.getCompoundTag(GUI_TAG);
		this.updateGrid();
		super.readFromNBT(compound);
		if (world != null)
			onLoad();
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
	
	private void updateWorld(final BlockPos newposition, final Chunk chunk) {
		final IBlockState state = world.getBlockState(newposition);
		world.markAndNotifyBlock(newposition, chunk, state, state, 3);
	}
	
	private void loadAndConfig(final int speed, final BlockPos lastPosition, final BlockPos newposition) {
		loadChunkAndGetTile(world, lastPosition, (oldtile, chunk) -> loadChunkAndGetTile(world, newposition, (newtile, _u2) -> {
			final Signal current = newtile.getSignal();
			final ISignalAutoconfig config = current.getConfig();
			if (config == null)
				return;
			config.change(speed, newtile, oldtile);
			updateWorld(newposition, chunk);
		}));
	}
	
	private void loadAndReset(final BlockPos position) {
		loadChunkAndGetTile(world, position, (signaltile, chunk) -> {
			final ISignalAutoconfig config = signaltile.getSignal().getConfig();
			if (config == null)
				return;
			config.reset(signaltile);
			updateWorld(position, chunk);
		});
	}
	
	private void lateUpdate(final BlockPos first) {
		if (first == null)
			return;
		BlockPos earlier = first;
		Entry<BlockPos, Integer> next;
		int debugX = 0;
		while ((next = pathWayEnd.get(earlier)) != null) {
			loadAndConfig(next.getValue(), earlier, next.getKey());
			earlier = next.getKey();
			if (earlier == null)
				return;
			if(debugX++ > 100)
				throw new RuntimeException("Too many iterations, this is a bug in debug!");
		}
	}
	
	private void resend() {
		final NBTTagCompound update = new NBTTagCompound();
		modeGrid.values().forEach(signal -> signal.write(update));
		this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
	}
	
	private void resetWay(final Point resetPoint) {
		final SignalNode currentNode = modeGrid.get(resetPoint);
		currentNode.getRotations(EnumGuiMode.HP).forEach(rotation -> {
			currentNode.applyNormal(Maps.immutableEntry(EnumGuiMode.HP, rotation), option -> {
				final BlockPos position = option.getLinkedPosition(LinkType.SIGNAL);
				if (position == null)
					return;
				final Entry<BlockPos, Integer> next = pathWayEnd.get(position);
				loadAndReset(position);
				lateUpdate(position);
				if (next != null)
					pathWayEnd.remove(next.getKey());
			});
			final List<Point> list = new ArrayList<>();
			final List<Point> visited = new ArrayList<>();
			list.add(SignalBoxUtil.getOffset(rotation, resetPoint));
			visited.add(resetPoint);
			while (!list.isEmpty()) {
				final Point current = list.get(0);
				final SignalNode nextPoint = modeGrid.get(current);
				if (nextPoint == null)
					return;
				visited.add(current);
				nextPoint.connections().forEach(entry -> nextPoint.apply(entry, path -> {
					setPower(path.getLinkedPosition(LinkType.OUTPUT), false);
					if (path.getPathUsage().equals(EnumPathUsage.FREE))
						return;
					path.setPathUsage(EnumPathUsage.FREE);
					if (!visited.contains(entry.getValue()))
						list.add(entry.getValue());
					if (!visited.contains(entry.getKey()))
						list.add(entry.getKey());
				}));
				list.remove(current);
			}
		});
		resend();
	}
	
	private void setPower(final BlockPos position, final boolean power) {
		if (position == null)
			return;
		loadChunkAndGetBlock(world, position, (state, chunk) -> {
			if(!(state.getBlock() instanceof RedstoneIO))
				return;
			final IBlockState ibstate = state.withProperty(RedstoneIO.POWER, power);
			chunk.setBlockState(position, ibstate);
			world.markAndNotifyBlock(position, chunk, state, ibstate, 3);
		});
	}
	
	public void onWayAdd(final ArrayList<SignalNode> nodes) {
		final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
		for (int i = 1; i < nodes.size() - 1; i++) {
			final Point oldPos = nodes.get(i - 1).getPoint();
			final Point newPos = nodes.get(i + 1).getPoint();
			final Entry<Point, Point> entry = Maps.immutableEntry(oldPos, newPos);
			final SignalNode current = nodes.get(i);
			current.apply(entry, option -> {
				setPower(option.getLinkedPosition(LinkType.OUTPUT), true);
				option.setPathUsage(EnumPathUsage.SELECTED);
				atomic.getAndUpdate(oldspeed -> Math.min(oldspeed, option.getSpeed()));
			});
		}
		final SignalNode firstNode = nodes.get(nodes.size() - 1);
		final SignalNode lastNode = nodes.get(0);
		final BlockPos lastPosition = lastNode.getOption(EnumGuiMode.HP).get().getLinkedPosition(LinkType.SIGNAL);
		final BlockPos firstPosition = firstNode.getOption(EnumGuiMode.HP).get().getLinkedPosition(LinkType.SIGNAL);
		if (lastPosition != null && firstPosition != null && !lastPosition.equals(firstPosition)) {
			loadAndConfig(atomic.get(), lastPosition, firstPosition);
			for (final SignalNode node : nodes) {
				node.getOption(EnumGuiMode.VP).ifPresent(option -> loadAndConfig(atomic.get(), lastPosition, option.getLinkedPosition(LinkType.SIGNAL)));
			}
			pathWayEnd.put(lastPosition, Maps.immutableEntry(firstPosition, atomic.get()));
		}
		lateUpdate(firstPosition);
		resend();
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		if (compound == null)
			return;
		if (compound.hasKey(RESET_WAY)) {
			final NBTTagCompound request = (NBTTagCompound) compound.getTag(RESET_WAY);
			final Point p1 = fromNBT(request, POINT1);
			resetWay(p1);
			return;
		}
		if (compound.hasKey(REQUEST_WAY)) {
			final NBTTagCompound request = (NBTTagCompound) compound.getTag(REQUEST_WAY);
			final Point p1 = fromNBT(request, POINT1);
			final Point p2 = fromNBT(request, POINT2);
			final Optional<ArrayList<SignalNode>> ways = requestWay(modeGrid, p1, p2);
			if (ways.isPresent()) {
				this.onWayAdd(ways.get());
			} else {
				final NBTTagCompound update = new NBTTagCompound();
				update.setString(ERROR_STRING, "error.nopathfound");
				this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
			}
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
	public boolean link(BlockPos linkedPos) {
		if (linkedBlocks.containsKey(linkedPos))
			return false;
		final IBlockState state = world.getBlockState(linkedPos);
		final Block block = state.getBlock();
		LinkType type = LinkType.SIGNAL;
		if (block == GIRBlocks.REDSTONE_IN) {
			type = LinkType.INPUT;
		} else if (block == GIRBlocks.REDSTONE_OUT) {
			type = LinkType.OUTPUT;
		}
		if (!world.isRemote) {
			if (type.equals(LinkType.SIGNAL)) {
				loadChunkAndGetTile(world, linkedPos, this::updateSingle);
			}
		}
		linkedBlocks.put(linkedPos, type);
		return true;
	}
	
	private void updateSingle(final SignalTileEnity signaltile, final Chunk _u) {
		final BlockPos signalPos = signaltile.getPos();
		signals.put(signalPos, signaltile.getSignal());
		syncClient();
	}
	
	@Override
	public void onLoad() {
		if (world.isRemote)
			return;
		signals.clear();
		new Thread(() -> {
			linkedBlocks.forEach((linkedPos, _u) -> loadChunkAndGetTile(world, linkedPos, this::updateSingle));
		}).start();
	}
	
	@Override
	public boolean unlink() {
		linkedBlocks.clear();
		signals.clear();
		syncClient();
		return true;
	}
	
	public Signal getSignal(final BlockPos pos) {
		return this.signals.get(pos);
	}
	
	public ImmutableMap<BlockPos, LinkType> getPositions() {
		return ImmutableMap.copyOf(this.linkedBlocks);
	}
	
}
