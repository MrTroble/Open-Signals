package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.SubsidiaryEntry;
import com.troblecodings.signals.core.WriteBuffer;
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

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalBox extends ContainerBase implements UIClientSync {

    protected Map<Point, Map<ModeSet, SubsidiaryEntry>> enabledSubsidiaryTypes = new HashMap<>();
    protected SignalBoxGrid grid;
    private final AtomicReference<Map<BlockPos, LinkType>> propertiesForType = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, String>> names = new AtomicReference<>();
    private final GuiInfo info;
    private SignalBoxTileEntity tile;
    private Consumer<String> run;
    private Consumer<List<SignalBoxNode>> colorUpdates;

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
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte((byte) SignalBoxNetwork.SEND_GRID.ordinal());
        buffer.putBlockPos(info.pos);
        grid.writeNetwork(buffer);
        final PosIdentifier identifier = new PosIdentifier(tile.getBlockPos(), info.world);
        final Map<BlockPos, LinkType> positions = SignalBoxHandler.getAllLinkedPos(identifier);
        buffer.putInt(positions.size());
        positions.forEach((pos, type) -> {
            buffer.putBlockPos(pos);
            buffer.putByte((byte) type.ordinal());
        });
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buffer);
        switch (mode) {
            case SEND_GRID: {
                final BlockPos pos = buffer.getBlockPos();
                if (this.tile == null) {
                    this.tile = (SignalBoxTileEntity) info.world.getBlockEntity(pos);
                }
                grid = tile.getSignalBoxGrid();
                grid.readNetwork(buffer);
                enabledSubsidiaryTypes = new HashMap<>(grid.getAllSubsidiaries());
                final int size = buffer.getInt();
                final Map<BlockPos, LinkType> allPos = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    final BlockPos blockPos = buffer.getBlockPos();
                    final LinkType type = LinkType.of(buffer);
                    allPos.put(blockPos, type);
                }
                propertiesForType.set(allPos);
                update();
                break;
            }
            case SEND_PW_UPDATE: {
                colorUpdates.accept(grid.readUpdateNetwork(buffer, false));
                break;
            }
            case NO_PW_FOUND: {
                run.accept(I18n.get("error.nopathfound"));
                break;
            }
            case NO_OUTPUT_UPDATE: {
                run.accept(I18n.get("error.nooutputupdate"));
                break;
            }
            case OUTPUT_UPDATE: {
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                final boolean state = buffer.getByte() == 1 ? true : false;
                final SignalBoxNode node = grid.getNode(point);
                if (state) {
                    node.addManuellOutput(modeSet);
                } else {
                    node.removeManuellOutput(modeSet);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        final SignalBoxGrid grid = tile.getSignalBoxGrid();
        final SignalBoxNetwork mode = SignalBoxNetwork.of(buffer);
        switch (mode) {
            case SEND_INT_ENTRY: {
                deserializeEntry(buffer, buffer.getByteAsInt());
                break;
            }
            case REMOVE_ENTRY: {
                final Point point = Point.of(buffer);
                final EnumGuiMode guiMode = EnumGuiMode.of(buffer);
                final Rotation rotation = deserializeRotation(buffer);
                final PathEntryType<?> entryType = PathEntryType.ALL_ENTRIES
                        .get(buffer.getByteAsInt());
                final ModeSet modeSet = new ModeSet(guiMode, rotation);
                grid.getNode(point).getOption(modeSet).ifPresent(entry -> {
                    entry.removeEntry(entryType);
                });
                break;
            }
            case SEND_POS_ENTRY: {
                deserializeEntry(buffer, buffer.getBlockPos());
                break;
            }
            case SEND_ZS2_ENTRY: {
                deserializeEntry(buffer, buffer.getByte());
                break;
            }
            case REMOVE_POS: {
                final BlockPos pos = buffer.getBlockPos();
                SignalBoxHandler.unlinkPosFromSignalBox(
                        new PosIdentifier(tile.getBlockPos(), tile.getLevel()), pos);
                break;
            }
            case RESET_PW: {
                final Point point = Point.of(buffer);
                grid.resetPathway(point);
                break;
            }
            case REQUEST_PW: {
                final Point start = Point.of(buffer);
                final Point end = Point.of(buffer);
                if (!grid.requestWay(start, end)) {
                    final WriteBuffer error = new WriteBuffer();
                    error.putByte((byte) SignalBoxNetwork.NO_PW_FOUND.ordinal());
                    OpenSignalsMain.network.sendTo(info.player, error.build());
                }
                break;
            }
            case RESET_ALL_PW: {
                grid.resetAllPathways();
                break;
            }
            case SEND_CHANGED_MODES: {
                grid.readUpdateNetwork(buffer, true);
                break;
            }
            case REQUEST_SUBSIDIARY: {
                final SubsidiaryEntry entry = SubsidiaryEntry.of(buffer);
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                grid.updateSubsidiarySignal(point, modeSet, entry);
                break;
            }
            case UPDATE_RS_OUTPUT: {
                final Point point = Point.of(buffer);
                final ModeSet modeSet = ModeSet.of(buffer);
                final boolean state = buffer.getByte() == 1 ? true : false;
                final BlockPos pos = grid.updateManuellRSOutput(point, modeSet, state);
                if (pos == null) {
                    final WriteBuffer error = new WriteBuffer();
                    error.putByte((byte) SignalBoxNetwork.NO_OUTPUT_UPDATE.ordinal());
                    OpenSignalsMain.network.sendTo(info.player, error.build());
                } else {
                    SignalBoxHandler.updateRedstoneOutput(new PosIdentifier(pos, info.world),
                            state);
                    final WriteBuffer sucess = new WriteBuffer();
                    sucess.putByte((byte) SignalBoxNetwork.OUTPUT_UPDATE.ordinal());
                    point.writeNetwork(sucess);
                    modeSet.writeNetwork(sucess);
                    sucess.putByte((byte) (state ? 1 : 0));
                    OpenSignalsMain.network.sendTo(info.player, sucess.build());
                }
                break;
            }
            case SET_AUTO_POINT: {
                final Point point = Point.of(buffer);
                final boolean state = buffer.getByte() == 1 ? true : false;
                final SignalBoxNode node = tile.getSignalBoxGrid().getNode(point);
                node.setAutoPoint(state);
                SignalBoxHandler.updatePathwayToAutomatic(
                        new PosIdentifier(tile.getBlockPos(), info.world), point);
                break;
            }
            case SEND_NAME: {
                final Point point = Point.of(buffer);
                final int size = buffer.getByteAsInt();
                final byte[] array = new byte[size];
                for (int i = 0; i < size; i++)
                    array[i] = buffer.getByte();
                tile.getSignalBoxGrid().getNode(point).setCustomText(new String(array));
                break;
            }
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void deserializeEntry(final ReadBuffer buffer, final T type) {
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

    private static Rotation deserializeRotation(final ReadBuffer buffer) {
        return Rotation.values()[buffer.getByteAsInt()];
    }

    @Override
    public void removed(final PlayerEntity playerIn) {
        super.removed(playerIn);
        if (this.tile != null)
            this.tile.remove(this);
    }

    @Override
    public PlayerEntity getPlayer() {
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
    public boolean stillValid(final PlayerEntity playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.info.player == null) {
            this.info.player = playerIn;
            this.tile.add(this);
        }
        return true;
    }

    protected void setConsumer(final Consumer<String> run) {
        this.run = run;
    }

    protected void setColorUpdater(final Consumer<List<SignalBoxNode>> updater) {
        this.colorUpdates = updater;
    }
}
