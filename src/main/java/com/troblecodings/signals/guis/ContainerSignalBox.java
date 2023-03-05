package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.BufferBuilder;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;

public class ContainerSignalBox extends ContainerBase implements UIClientSync {

    public final static String UPDATE_SET = "update";
    public final static String SIGNAL_ID = "signal";
    public final static String POS_ID = "posid";
    public final static String SIGNAL_NAME = "signalName";

    private final AtomicReference<Map<BlockPos, LinkType>> propertiesForType = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, String>> names = new AtomicReference<>();
    private SignalBoxTileEntity tile;
    private Consumer<NBTWrapper> run;
    private final GuiInfo info;
    protected SignalBoxGrid grid;
    protected boolean planeUpdate = false;

    public ContainerSignalBox(final GuiInfo info) {
        super(info);
        if (!info.world.isClientSide) {
            this.tile = info.getTile();
            tile.add(this);
        }
        info.player.containerMenu = this;
        this.info = info;
    }

    @Override
    public void sendAllDataToRemote() {
        final SignalBoxGrid grid = tile.getSignalBoxGrid();
        final BufferBuilder buffer = new BufferBuilder();
        buffer.putByte((byte) SignalBoxNetwork.SEND_GRID.ordinal());
        buffer.putInt(info.pos.getX());
        buffer.putInt(info.pos.getY());
        buffer.putInt(info.pos.getZ());
        grid.writeToBuffer(buffer);
        final Map<BlockPos, LinkType> positions = tile.getPositions();
        buffer.putInt(positions.size());
        positions.forEach((pos, type) -> {
            buffer.putInt(pos.getX());
            buffer.putInt(pos.getY());
            buffer.putInt(pos.getZ());
            buffer.putByte((byte) type.ordinal());
        });
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    public ContainerSignalBox(final GuiInfo info, final Consumer<NBTWrapper> run) {
        this(info);
        this.run = run;
    }

    @Override
    public void removed(final Player playerIn) {
        super.removed(playerIn);
        if (this.tile != null)
            this.tile.remove(this);
    }

    @Override
    public Player getPlayer() {
        return this.info.player;
    }

    public Map<BlockPos, Signal> getProperties() {
        return this.properties.get();
    }

    public Map<BlockPos, String> getNames() {
        return this.names.get();
    }

    public Map<BlockPos, LinkType> getPositionForTypes() {
        return this.propertiesForType.get();
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.info.player == null) {
            this.info.player = playerIn;
            this.tile.add(this);
        }
        return true;
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buf);
        if (mode.equals(SignalBoxNetwork.SEND_GRID)) {
            final BlockPos pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
            if (this.tile == null) {
                this.tile = (SignalBoxTileEntity) info.world.getBlockEntity(pos);
            }
            grid = tile.getSignalBoxGrid();
            grid.readNetwork(buf);
            final int size = buf.getInt();
            final Map<BlockPos, LinkType> allPos = new HashMap<>();
            for (int i = 0; i < size; i++) {
                final BlockPos blockPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
                final LinkType type = LinkType.of(buf);
                allPos.put(blockPos, type);
            }
            propertiesForType.set(allPos);
            update();
        }
        if (mode.equals(SignalBoxNetwork.SEND_PW_UPDATE)) {
            grid.readUpdateNetwork(buf);
            update();
        }
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buf);
        if (mode.equals(SignalBoxNetwork.SEND_INT_ENTRY)) {
            deserializeEntry(buf, Byte.toUnsignedInt(buf.get()));
            return;
        }
        if (mode.equals(SignalBoxNetwork.REMOVE_ENTRY)) {
            final Point point = new Point(buf);
            final EnumGuiMode guiMode = EnumGuiMode.of(buf);
            final Rotation rotation = deserializeRotation(buf);
            final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES
                    .get(Byte.toUnsignedInt(buf.get()));
            final ModeSet modeSet = new ModeSet(guiMode, rotation);
            tile.getSignalBoxGrid().getNode(point).getOption(modeSet).ifPresent(entry -> {
                entry.removeEntry(entryType);
            });
        }
        if (mode.equals(SignalBoxNetwork.SEND_POS_ENTRY)) {
            deserializeEntry(buf, deserializeBlockPos(buf));
            return;
        }
        if (mode.equals(SignalBoxNetwork.REMOVE_POS)) {
            final LinkType type = LinkType.of(buf);
            final BlockPos pos = deserializeBlockPos(buf);
            tile.removeLinkedPos(pos);
            if (type.equals(LinkType.SIGNAL)) {
                tile.removeSignal(pos);
            }
            return;
        }
        if (mode.equals(SignalBoxNetwork.RESET_PW)) {
            final Point point = new Point(buf);
            tile.getSignalBoxGrid().resetPathway(point);
            return;
        }
        if (mode.equals(SignalBoxNetwork.REQUEST_PW)) {
            final Point start = new Point(buf);
            final Point end = new Point(buf);
            tile.getSignalBoxGrid().requestWay(start, end);
            return;
        }
        if (mode.equals(SignalBoxNetwork.RESET_ALL_PW)) {
            tile.getSignalBoxGrid().resetAllPathways();
            return;
        }
        if (mode.equals(SignalBoxNetwork.SEND_CHANGED_MODES)) {
            tile.getSignalBoxGrid().readNetwork(buf);
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void deserializeEntry(final ByteBuffer buffer, final T type) {
        final Point point = new Point(buffer);
        final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
        final Rotation rotation = deserializeRotation(buffer);
        final PathEntryType<T> entryType = (PathEntryType<T>) PathEntryType.ALL_ENTRIES
                .get(buffer.get());
        final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
        final ModeSet modeSet = new ModeSet(guiMode, rotation);
        final Optional<PathOptionEntry> option = node.getOption(modeSet);
        if (option.isPresent()) {
            option.get().setEntry(entryType, type);
        } else {
            node.addAndSetEntry(modeSet, entryType, type);
        }
    }

    private static Rotation deserializeRotation(final ByteBuffer buffer) {
        return Rotation.values()[Byte.toUnsignedInt(buffer.get())];
    }

    private static BlockPos deserializeBlockPos(final ByteBuffer buffer) {
        return new BlockPos(buffer.getInt(), buffer.getInt(), buffer.getInt());
    }
}
