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
import eu.gir.girsignals.signalbox.config.RSSignalConfig;
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
	public static final String REMOVE_SIGNAL = "removeSignal";

	private static final String LINKED_POS_LIST = "linkedPos";
	private static final String GUI_TAG = "guiTag";
	private static final String LINK_TYPE = "linkType";
	
	private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>(10);
	private final Map<Point, SignalNode> modeGrid = new HashMap<>(10);
	private final Map<BlockPos, Signal> signals = new HashMap<>(10);
	private NBTTagCompound guiTag = new NBTTagCompound();
	private final HashMap<ArrayList<SignalNode>, Integer> pathWayEnd = new HashMap<>();
	
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
				if (item.hasKey(LINK_TYPE))
					linkedBlocks.put(NBTUtil.getPosFromTag(item), LinkType.valueOf(item.getString(LINK_TYPE)));
			});
		}
		this.guiTag = compound.getCompoundTag(GUI_TAG);
		this.updateModeGridFromUI();
		super.readFromNBT(compound);
		if (world != null)
			onLoad();
	}
	
	private void updateModeGridFromUI() {
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
	
	private void notifyBlockChanges(final BlockPos newposition, final Chunk chunk) {
		final IBlockState state = world.getBlockState(newposition);
		world.markAndNotifyBlock(newposition, chunk, state, state, 3);
	}
	
	private void loadAndConfig(final int speed, final BlockPos lastPosition, final BlockPos newposition) {
		loadAndConfig(speed, lastPosition, newposition, null);
	}
	
	private void loadAndConfig(final int speed, final BlockPos lastPosition, final BlockPos newposition, final ISignalAutoconfig override) {
		loadChunkAndGetTile(world, lastPosition, (oldtile, chunk) -> loadChunkAndGetTile(world, newposition, (newtile, _u2) -> {
			final Signal current = newtile.getSignal();
			final ISignalAutoconfig config = override == null ? current.getConfig():override;
			if (config == null)
				return;
			config.change(speed, newtile, oldtile);
			notifyBlockChanges(newposition, chunk);
		}));
	}
	
	private void loadAndReset(final BlockPos position) {
		loadChunkAndGetTile(world, position, (signaltile, chunk) -> {
			final ISignalAutoconfig config = signaltile.getSignal().getConfig();
			if (config == null)
				return;
			config.reset(signaltile);
			notifyBlockChanges(position, chunk);
		});
	}
	
	private void resendSignalTilesToUI() {
		final NBTTagCompound update = new NBTTagCompound();
		modeGrid.values().forEach(signal -> signal.write(update));
		this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
	}
	
	private void resetPathway(final Point resetPoint) {
		final SignalNode currentNode = modeGrid.get(resetPoint);
		resetSignal(resetPoint, currentNode, EnumGuiMode.HP);
		resetSignal(resetPoint, currentNode, EnumGuiMode.RS);
		pathWayEnd.keySet().stream().filter(list -> list.get(list.size() - 1).equals(currentNode)).findAny().ifPresent(pathWayEnd::remove);
		resendSignalTilesToUI();
	}
	
	private void resetSignal(final Point resetPoint, final SignalNode currentNode, final EnumGuiMode guiMode) {
		currentNode.getRotations(guiMode).forEach(rotation -> {
			currentNode.applyNormal(Maps.immutableEntry(guiMode, rotation), option -> {
				final BlockPos position = option.getLinkedPosition(LinkType.SIGNAL);
				if (position == null)
					return;
				loadAndReset(position);
			});
			pathWayEnd.keySet().stream().filter(list -> list.get(0).equals(currentNode)).findAny().ifPresent(list -> {
				setPathway(list, pathWayEnd.get(list));
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
	}
	
	private void setPower(final BlockPos position, final boolean power) {
		if (position == null)
			return;
		loadChunkAndGetBlock(world, position, (state, chunk) -> {
			if (!(state.getBlock() instanceof RedstoneIO))
				return;
			final IBlockState ibstate = state.withProperty(RedstoneIO.POWER, power);
			chunk.setBlockState(position, ibstate);
			world.markAndNotifyBlock(position, chunk, state, ibstate, 3);
		});
	}
	
	private void onWayAdd(final ArrayList<SignalNode> nodes) {
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
		setPathway(nodes, atomic.get());
		pathWayEnd.put(nodes, atomic.get());
		resendSignalTilesToUI();
	}
	
	private void setPathway(final ArrayList<SignalNode> nodes, final int speed) {
		final SignalNode firstNode = nodes.get(nodes.size() - 1);
		final SignalNode lastNode = nodes.get(0);
		final Optional<PathOption> lPO = lastNode.getOption(EnumGuiMode.HP);
		final Optional<PathOption> fPO = firstNode.getOption(EnumGuiMode.HP);
		if (fPO.isPresent()) {
			final BlockPos lastPosition = lPO.isPresent() ? lPO.get().getLinkedPosition(LinkType.SIGNAL):null;
			final BlockPos firstPosition = fPO.get().getLinkedPosition(LinkType.SIGNAL);
			if (firstPosition != null && !lastPosition.equals(firstPosition)) {
				loadAndConfig(speed, lastPosition, firstPosition);
				for (final SignalNode node : nodes) {
					node.getOption(EnumGuiMode.VP).ifPresent(option -> loadAndConfig(speed, lastPosition, option.getLinkedPosition(LinkType.SIGNAL)));
				}
			}
		} else {
			final Optional<PathOption> lRSPO = lastNode.getOption(EnumGuiMode.RS);
			final Optional<PathOption> fRSPO = firstNode.getOption(EnumGuiMode.RS);
			if (fRSPO.isPresent()) {
				final BlockPos lastPosition = lRSPO.get().getLinkedPosition(LinkType.SIGNAL);
				if (lastPosition != null) {
					loadAndConfig(speed, lastPosition, null, RSSignalConfig.RS_CONFIG);
				}
			}
			return;
		}
		pathWayEnd.keySet().stream().filter(list -> list != null && list.get(0).equals(firstNode)).findAny().ifPresent(list -> setPathway(list, pathWayEnd.get(list)));
	}
	
	@Override
	public void updateTag(NBTTagCompound compound) {
		if (compound == null)
			return;
		if (compound.hasKey(REMOVE_SIGNAL)) {
			final NBTTagCompound request = (NBTTagCompound) compound.getTag(REMOVE_SIGNAL);
			final BlockPos p1 = NBTUtil.getPosFromTag(request);
			if(signals.containsKey(p1)) {
				signals.remove(p1);
				loadAndReset(p1);
			}
			linkedBlocks.remove(p1);
		}
		if (compound.hasKey(RESET_WAY)) {
			final NBTTagCompound request = (NBTTagCompound) compound.getTag(RESET_WAY);
			final Point p1 = fromNBT(request, POINT1);
			resetPathway(p1);
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
		updateModeGridFromUI();
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
				loadAndReset(linkedPos);
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
		signals.keySet().forEach(this::loadAndReset);
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
