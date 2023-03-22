package com.troblecodings.signals.signalbox;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbox.config.SignalConfig;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
import com.troblecodings.signals.tileentitys.SyncableTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

    public static final String ERROR_STRING = "error";
    public static final String REMOVE_SIGNAL = "removeSignal";

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String LINKED_SIGNALS = "linkedSignals";
    private static final String SIGNAL_NAME = "signalName";

    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final SignalBoxGrid grid;

    public SignalBoxTileEntity(final TileEntityInfo info) {
        super(info);
        grid = SignalBoxFactory.getFactory().getGrid();
    }

    @Override
    public void setLevel(final Level world) {
        super.setLevel(world);
        if (world.isClientSide)
           return;
        grid.setTile(this);
        SignalBoxHandler.setWorld(worldPosition, world);
    }

    private final WorldOperations worldLoadOps = new WorldOperations();

    public void removeSignal(final BlockPos pos) {
        if (level.isClientSide)
            return;
        SignalConfig.reset(
                new SignalStateInfo(level, pos, SignalBoxHandler.removeSignal(worldPosition, pos)));
    }

    public void removeLinkedPos(final BlockPos pos) {
        if (level.isClientSide)
            return;
        linkedBlocks.remove(pos);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_POS_LIST, linkedBlocks.entrySet().stream().map(entry -> {
            final NBTWrapper item = NBTWrapper.getBlockPosWrapper(entry.getKey());
            entry.getValue().write(item);
            return item;
        })::iterator);
        final Map<BlockPos, Signal> allSignals = SignalBoxHandler.containsTilePos(worldPosition)
                ? SignalBoxHandler.getSignals(worldPosition)
                : new HashMap<>();
        wrapper.putList(LINKED_SIGNALS, allSignals.entrySet().stream().map(entry -> {
            final NBTWrapper signal = NBTWrapper.getBlockPosWrapper(entry.getKey());
            signal.putString(SIGNAL_NAME, entry.getValue().getSignalTypeName());
            return signal;
        })::iterator);
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        SignalBoxHandler.writeToNBT(worldPosition, gridTag);
        wrapper.putWrapper(GUI_TAG, gridTag);
    }

    private NBTWrapper copy = null;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedBlocks.clear();
        SignalBoxHandler.clearSignals(worldPosition);
        wrapper.getList(LINKED_POS_LIST)
                .forEach(nbt -> linkedBlocks.put(nbt.getAsPos(), LinkType.of(nbt)));
        final Map<BlockPos, Signal> allSignals = new HashMap<>();
        wrapper.getList(LINKED_SIGNALS).forEach(nbt -> {
            final BlockPos pos = nbt.getAsPos();
            final Signal signal = Signal.SIGNALS.get(nbt.getString(SIGNAL_NAME));
            allSignals.put(pos, signal);
        });
        SignalBoxHandler.setSignals(worldPosition, allSignals);
        grid.read(wrapper.getWrapper(GUI_TAG));
        copy = wrapper.copy();
        if (level != null) {
            onLoad();
        }
    }

    @Override
    public boolean hasLink() {
        return !linkedBlocks.isEmpty();
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        if (linkedBlocks.containsKey(pos))
            return false;
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(new ResourceLocation(OpenSignalsMain.MODID,
                tag.getString(SignalControllerTileEntity.SIGNAL_NAME)));
        if (block == null || block instanceof AirBlock)
            return false;
        LinkType type = LinkType.SIGNAL;
        if (block == OSBlocks.REDSTONE_IN) {
            type = LinkType.INPUT;
        } else if (block == OSBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (type.equals(LinkType.SIGNAL)) {
            SignalConfig.reset(new SignalStateInfo(level, pos, (Signal) block));
            SignalBoxHandler.addSignal(worldPosition, (Signal) block, pos);
        }
        linkedBlocks.put(pos, type);
        return true;

    }

    @Override
    public void onLoad() {
        if (level.isClientSide) {
            return;
        }
        grid.setTile(this);
        final GridComponent component = SignalBoxHandler.computeIfAbsent(worldPosition, level);
        component.read(copy == null ? new NBTWrapper() : copy.getWrapper(GUI_TAG),
                grid.getModeGrid());
    }

    @Override
    public boolean unlink() {
        final Map<BlockPos, Signal> signals = SignalBoxHandler.containsTilePos(worldPosition)
                ? SignalBoxHandler.clearSignals(worldPosition)
                : new HashMap<>();
        signals.forEach(
                (pos, signal) -> SignalConfig.reset(new SignalStateInfo(level, pos, signal)));
        linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue()))
                .forEach(entry -> {
                });
        linkedBlocks.clear();
        syncClient();
        return true;
    }

    public Map<BlockPos, LinkType> getPositions() {
        return ImmutableMap.copyOf(this.linkedBlocks);
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

    public SignalBoxGrid getSignalBoxGrid() {
        return grid;
    }
}
