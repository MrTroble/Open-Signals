package com.troblecodings.signals.signalbox;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.SignalTileEntity;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

	public static final String ERROR_STRING = "error";
	public static final String REMOVE_SIGNAL = "removeSignal";

	private static final String LINKED_POS_LIST = "linkedPos";

	private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
	private final Map<BlockPos, Signal> signals = new HashMap<>();
	private final SignalBoxGrid grid;

	public SignalBoxTileEntity(final TileEntityInfo info) {
		super(info);
		grid = SignalBoxFactory.getFactory().getGrid(this::sendToAll);
	}

	private WorldOperations worldLoadOps = new WorldOperations();

	@Override
	public void setLevel(final Level worldIn) {
		super.setLevel(worldIn);
		// TODO World ops?
	}

	@Override
	public void saveWrapper(NBTWrapper wrapper) {
		wrapper.putList(LINKED_POS_LIST, linkedBlocks.entrySet().stream().map(entry -> {
			final NBTWrapper item = NBTWrapper.getBlockPosWrapper(entry.getKey());
			entry.getValue().write(item);
			return item;
		})::iterator);
		final NBTWrapper gridTag = new NBTWrapper();
		this.grid.write(gridTag);
		wrapper.putWrapper(GUI_TAG, gridTag);
	}

	@Override
	public void loadWrapper(NBTWrapper wrapper) {
		linkedBlocks.clear();
		wrapper.getList(LINKED_POS_LIST).forEach(nbt -> linkedBlocks.put(nbt.getAsPos(), LinkType.of(nbt)));
		grid.read(wrapper.getWrapper(GUI_TAG));
		if (level != null) {
			onLoad();
		}
	}

	@Override
	public boolean hasLink() {
		return !linkedBlocks.isEmpty();
	}

	@Override
	public boolean link(final BlockPos linkedPos) {
		if (linkedBlocks.containsKey(linkedPos))
			return false;
		final BlockState state = level.getBlockState(linkedPos);
		final Block block = state.getBlock();
		LinkType type = LinkType.SIGNAL;
		if (block == OSBlocks.REDSTONE_IN) {
			type = LinkType.INPUT;
		} else if (block == OSBlocks.REDSTONE_OUT) {
			type = LinkType.OUTPUT;
		}
		if (level.isClientSide) {
			if (type.equals(LinkType.SIGNAL)) {
				worldLoadOps.loadAndReset(linkedPos);
			}
		}
		linkedBlocks.put(linkedPos, type);
		this.syncClient();
		return true;
	}

	private void updateSingle(final SignalTileEntity signaltile, final LevelChunk unused) {
		final BlockPos signalPos = signaltile.getBlockPos();
		signals.put(signalPos, signaltile.getSignal());
		syncClient();
	}

	@Override
	public void onLoad() {
		if (!level.isClientSide)
			return;
		signals.clear();
	}

	@Override
	public boolean unlink() {
		signals.keySet().forEach(worldLoadOps::loadAndReset);
		linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue())).forEach(entry -> {
		});
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

	public void updateRedstonInput(final BlockPos pos, final boolean power) {
		if (power && this.level.isClientSide) {
			grid.setPowered(pos);
			syncClient();
		}
	}

	public boolean isBlocked() {
		return !this.clientSyncs.isEmpty();
	}

	@Override
	public boolean isValid(final Player player) {
		if (clientSyncs.isEmpty())
			return false;
		return this.clientSyncs.get(0).getPlayer().equals(player);
	}
}
