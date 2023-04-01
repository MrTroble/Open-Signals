package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.BufferFactory;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.enums.SignalBoxNetwork;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.client.resources.language.I18n;
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
    private final GuiInfo info;
    protected SignalBoxGrid grid;
    private Consumer<String> run;
    protected Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();

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
        final BufferFactory buffer = new BufferFactory();
        buffer.putByte((byte) SignalBoxNetwork.SEND_GRID.ordinal());
        buffer.putBlockPos(info.pos);
        grid.writeNetwork(buffer);
        final Map<BlockPos, LinkType> positions = SignalBoxHandler
                .getAllLinkedPos(tile.getBlockPos());
        buffer.putInt(positions.size());
        positions.forEach((pos, type) -> {
            buffer.putBlockPos(pos);
            buffer.putByte((byte) type.ordinal());
        });
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BufferFactory buffer = new BufferFactory(buf);
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buffer);
        if (mode.equals(SignalBoxNetwork.SEND_GRID)) {
            final BlockPos pos = buffer.getBlockPos();
            if (this.tile == null) {
                this.tile = (SignalBoxTileEntity) info.world.getBlockEntity(pos);
            }
            grid = tile.getSignalBoxGrid();
            grid.readNetwork(buffer);
            enabledSubsidiaryTypes = grid.getAllSubsidiaries();
            final int size = buffer.getInt();
            final Map<BlockPos, LinkType> allPos = new HashMap<>();
            for (int i = 0; i < size; i++) {
                final BlockPos blockPos = buffer.getBlockPos();
                final LinkType type = LinkType.of(buffer);
                allPos.put(blockPos, type);
            }
            propertiesForType.set(allPos);
            update();
            return;
        }
        if (mode.equals(SignalBoxNetwork.SEND_PW_UPDATE)) {
            grid.readUpdateNetwork(buffer, false);
            update();
            return;
        }
        if (mode.equals(SignalBoxNetwork.NO_PW_FOUND)) {
            if (run != null) {
                run.accept(I18n.get("error.nopathfound"));
                return;
            }
        }
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final BufferFactory buffer = new BufferFactory(buf);
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buffer);
        if (mode.equals(SignalBoxNetwork.SEND_INT_ENTRY)) {
            deserializeEntry(buffer, buffer.getByteAsInt());
            return;
        }
        if (mode.equals(SignalBoxNetwork.REMOVE_ENTRY)) {
            final Point point = Point.of(buffer);
            final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
            final Rotation rotation = deserializeRotation(buffer);
            final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES.get(buffer.getByteAsInt());
            final ModeSet modeSet = new ModeSet(guiMode, rotation);
            tile.getSignalBoxGrid().getNode(point).getOption(modeSet).ifPresent(entry -> {
                entry.removeEntry(entryType);
            });
        }
        if (mode.equals(SignalBoxNetwork.SEND_POS_ENTRY)) {
            deserializeEntry(buffer, buffer.getBlockPos());
            return;
        }
        if (mode.equals(SignalBoxNetwork.SEND_ZS2_ENTRY)) {
            deserializeEntry(buffer, buffer.getByte());
            return;
        }
        if (mode.equals(SignalBoxNetwork.REMOVE_POS)) {
            final BlockPos pos = buffer.getBlockPos();
            SignalBoxHandler.removeLinkedPos(tile.getBlockPos(), pos);
            return;
        }
        if (mode.equals(SignalBoxNetwork.RESET_PW)) {
            final Point point = Point.of(buffer);
            tile.getSignalBoxGrid().resetPathway(point);
            return;
        }
        if (mode.equals(SignalBoxNetwork.REQUEST_PW)) {
            final Point start = Point.of(buffer);
            final Point end = Point.of(buffer);
            if (!tile.getSignalBoxGrid().requestWay(start, end)) {
                final BufferFactory error = new BufferFactory();
                error.putByte((byte) SignalBoxNetwork.NO_PW_FOUND.ordinal());
                OpenSignalsMain.network.sendTo(info.player, error.build());
            }
            return;
        }
        if (mode.equals(SignalBoxNetwork.RESET_ALL_PW)) {
            tile.getSignalBoxGrid().resetAllPathways();
            return;
        }
        if (mode.equals(SignalBoxNetwork.SEND_CHANGED_MODES)) {
            tile.getSignalBoxGrid().readUpdateNetwork(buffer, true);
            return;
        }
        if (mode.equals(SignalBoxNetwork.REQUEST_SUBSIDIARY)) {
            final SubsidiaryEntry entry = SubsidiaryEntry.of(buffer);
            final Point point = Point.of(buffer);
            final ModeSet modeSet = ModeSet.of(buffer);
            tile.getSignalBoxGrid().updateSubsidiarySignal(point, modeSet, entry);
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void deserializeEntry(final BufferFactory buffer, final T type) {
        final Point point = Point.of(buffer);
        final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
        final Rotation rotation = deserializeRotation(buffer);
        final PathEntryType<T> entryType = (PathEntryType<T>) PathEntryType.ALL_ENTRIES
                .get(buffer.getByteAsInt());
        final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
        final ModeSet modeSet = new ModeSet(guiMode, rotation);
        final Optional<PathOptionEntry> option = node.getOption(modeSet);
        if (option.isPresent()) {
            option.get().setEntry(entryType, type);
        } else {
            node.addAndSetEntry(modeSet, entryType, type);
        }
    }

    private static Rotation deserializeRotation(final BufferFactory buffer) {
        return Rotation.values()[buffer.getByteAsInt()];
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

    public void setConsumer(final Consumer<String> run) {
        this.run = run;
    }
}
