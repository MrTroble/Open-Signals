package com.troblecodings.signals.signalbox;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RedstonePacket;
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
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;

public class SignalBoxTileEntity extends SyncableTileEntity implements ISyncable, ILinkableTile {

    public static final String ERROR_STRING = "error";
    public static final String REMOVE_SIGNAL = "removeSignal";

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String LINKED_SIGNALS = "linkedSignals";
    private static final String SIGNAL_NAME = "signalName";

    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final Map<BlockPos, Signal> signals = new HashMap<>();
    private final SignalBoxGrid grid;

    public SignalBoxTileEntity(final TileEntityInfo info) {
        super(info);
        grid = SignalBoxFactory.getFactory().getGrid(this::sendToAll);
    }

    private final WorldOperations worldLoadOps = new WorldOperations();

    public void removeSignal(final BlockPos pos) {
        if (level.isClientSide)
            return;
        SignalConfig.reset(new SignalStateInfo(level, pos, signals.remove(pos)));
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
        wrapper.putList(LINKED_SIGNALS, signals.entrySet().stream().map(entry -> {
            final NBTWrapper signal = NBTWrapper.getBlockPosWrapper(entry.getKey());
            signal.putString(SIGNAL_NAME, entry.getValue().getSignalTypeName());
            return signal;
        })::iterator);
        final NBTWrapper gridTag = new NBTWrapper();
        this.grid.write(gridTag);
        wrapper.putWrapper(GUI_TAG, gridTag);
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedBlocks.clear();
        signals.clear();
        wrapper.getList(LINKED_POS_LIST)
                .forEach(nbt -> linkedBlocks.put(nbt.getAsPos(), LinkType.of(nbt)));
        wrapper.getList(LINKED_SIGNALS).forEach(
                nbt -> signals.put(nbt.getAsPos(), Signal.SIGNALS.get(nbt.getString(SIGNAL_NAME))));
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
            signals.put(pos, (Signal) block);
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
        final Optional<LinkedList<RedstonePacket>> updates = SignalBoxHandler
                .getPacket(worldPosition);
        if (!updates.isPresent()) {
            return;
        }
        RedstonePacket packet;
        while ((packet = updates.get().poll()) != null) {
            if (packet.world.equals(level)) {
                updateRedstonInput(packet.pos, packet.state);
            }
        }
    }

    @Override
    public boolean unlink() {
        signals.keySet().forEach(worldLoadOps::loadAndReset);
        linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue()))
                .forEach(entry -> {
                });
        linkedBlocks.clear();
        signals.clear();
        syncClient();
        return true;
    }

    public Signal getSignal(final BlockPos pos) {
        return this.signals.get(pos);
    }

    public Map<BlockPos, LinkType> getPositions() {
        return ImmutableMap.copyOf(this.linkedBlocks);
    }

    public void updateRedstonInput(final BlockPos pos, final boolean power) {
        if (power && !this.level.isClientSide) {
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

    public SignalBoxGrid getSignalBoxGrid() {
        return grid;
    }
}
