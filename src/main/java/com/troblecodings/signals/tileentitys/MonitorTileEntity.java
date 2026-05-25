package com.troblecodings.signals.tileentitys;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.opensignals.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.MonitorTEBlock;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkMode;
import com.troblecodings.signals.network.SignalBoxNetworkReader;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class MonitorTileEntity extends SyncableTileEntity
        implements ILinkableTile, SignalBoxNetworkReader {

    public static final String LINKED_SIGNAL_BOX = "linkedSignalBox";
    public static final String MONITOR_SIZE_X = "monitorSizeX";
    public static final String MONITOR_SIZE_Y = "monitorSizeY";
    public static final String RENDER_START_POINT = "renderStartPoint";
    public static final String RENDER_END_POINT = "renderEndPoint";

    private static final List<SignalBoxNetworkMode> allowedNetworkModes = ImmutableList.of(
            SignalBoxNetworkHandler.GRID, SignalBoxNetworkHandler.ENTRY,
            SignalBoxNetworkHandler.NODE_SPECIAL_ENTRIES, SignalBoxNetworkHandler.SUBSIDIARY);

    private final SignalBoxGrid grid = new SignalBoxGrid();
    private final Map<Point, Point> translatedPoints = new HashMap<>();
    private SignalBoxNetworkHandler network;
    private BlockPos linkedSignalBox = BlockPos.ZERO;
    private int monitorSizeX, monitorSizeY;
    private Point renderStart = new Point(-1, -1), renderEnd = new Point(-1, -1);
    private UISignalBoxRendering rendering;

    public MonitorTileEntity(final TileEntityInfo info) {
        super(info);
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedSignalBox = wrapper.getBlockPos(LINKED_SIGNAL_BOX);
        monitorSizeX = wrapper.getInteger(MONITOR_SIZE_X);
        monitorSizeY = wrapper.getInteger(MONITOR_SIZE_Y);
        renderStart = getPointFromWrapper(RENDER_START_POINT, wrapper);
        renderEnd = getPointFromWrapper(RENDER_END_POINT, wrapper);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putBlockPos(LINKED_SIGNAL_BOX, linkedSignalBox);
        wrapper.putInteger(MONITOR_SIZE_X, monitorSizeX);
        wrapper.putInteger(MONITOR_SIZE_Y, monitorSizeY);
        savePointOnWrapper(RENDER_START_POINT, renderStart, wrapper);
        savePointOnWrapper(RENDER_END_POINT, renderEnd, wrapper);
    }

    private void savePointOnWrapper(final String key, final Point point, final NBTWrapper wrapper) {
        final NBTWrapper pointWrapper = new NBTWrapper();
        point.write(pointWrapper);
        wrapper.putWrapper(key, pointWrapper);
    }

    private Point getPointFromWrapper(final String key, final NBTWrapper wrapper) {
        final NBTWrapper pointWrapper = wrapper.getWrapper(key);
        if (pointWrapper.isTagNull())
            return new Point(-1, -1);
        return Point.of(pointWrapper);
    }

    public void render(final RenderAnimationInfo info) {
        final Block block = getBlockState().getBlock();
        if (!(block instanceof MonitorTEBlock) || rendering == null)
            return;
        ((MonitorTEBlock) block).render(info, this, rendering);
    }

    @Override
    public boolean hasLink() {
        return linkedSignalBox != null && !linkedSignalBox.equals(BlockPos.ZERO);
    }

    @Override
    public boolean unlink() {
        MonitorNetworkHandler.deregisterMonitorFromBox(this);
        linkedSignalBox = BlockPos.ZERO;
        return true;
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toShortString())));
        if (block instanceof SignalBox) {
            linkedSignalBox = pos;
            MonitorNetworkHandler.registerMonitorToBox(this);
            return true;
        }
        return false;
    }

    public void loadBoxUpdate(final ReadBuffer buffer) {
        final boolean wasNetworkNull = network == null;
        if (wasNetworkNull) {
            network = new SignalBoxNetworkHandler(this, grid);
        }
        network.desirializeBuffer(buffer, allowedNetworkModes);
        if (wasNetworkNull) {
            initRendering();
        }
    }

    public void loadRenderPoints(final ReadBuffer buffer) {
        renderStart = buffer.getINetworkSaveable(Point.class);
        renderEnd = buffer.getINetworkSaveable(Point.class);
        initRendering();
    }

    private void initRendering() {
        rendering = new UISignalBoxRendering(grid, getProfile(),
                UISignalBoxProfile.DEFAULT.getOperationModeSettings().getUIBorderSettings(),
                (_u1, _u2, _u3) -> {
                }, new UIEntity(), new HashMap<>());
        updateRendering();
    }

    private void updateRendering() {
        translatedPoints.clear();
        if (rendering == null)
            return;
        final Map<Point, SignalBoxNode> nodes = new HashMap<>();
        for (final SignalBoxNode node : grid.getNodes()) {
            final Point p = node.getPoint();
            final Point translated = getPointTranslated(p);
            if (translated != null)
                nodes.put(translated, node);
        }
        nodes.forEach((p, n) -> {
            rendering.updateNode(p, n);
            handleNodeUpdate(n, PathEntryType.TRAINNUMBER);
            handleNodeUpdate(n, PathEntryType.PATHUSAGE);
        });
        buildColors(nodes.values(), getProfile());
    }

    private Point getPointTranslated(final Point p) {
        if (translatedPoints.containsKey(p))
            return translatedPoints.get(p);
        if (renderStart.getX() <= p.getX() && p.getX() <= renderEnd.getX()
                && renderStart.getY() <= p.getY() && p.getY() <= renderEnd.getY()) {
            final Point translated =
                    new Point(p.getX() - renderStart.getX(), p.getY() - renderStart.getY());
            translatedPoints.put(p, translated);
            return translated;
        }
        return null;
    }

    private void buildColors(final Collection<SignalBoxNode> nodes,
            final UISignalBoxProfile profile) {
        nodes.forEach(node -> {
            final Point translated = getPointTranslated(node.getPoint());
            if (translated == null)
                return;
            this.rendering.setColor(translated, mode -> {
                if (mode.mode == EnumGuiMode.TRAIN_NUMBER)
                    return profile.getOperationModeSettings().getTrainnumberBackgroundColor();
                if (node.containsManuellOutput(mode))
                    return profile.getOperationModeSettings().getOutputColor();
                return node.getOption(mode).get().getEntry(PathEntryType.PATHUSAGE)
                        .orElseGet(() -> EnumPathUsage.FREE)
                        .getColor(profile.getOperationModeSettings());
            });
        });
    }

    @Override
    public void handleNodeUpdate(final SignalBoxNode node, final PathEntryType<?> entryType) {
        if (entryType.equals(PathEntryType.TRAINNUMBER)) {
            node.iterator().forEachRemaining(modeSet -> {
                if (!(modeSet.mode == EnumGuiMode.TRAIN_NUMBER))
                    return;
                final Point translated = getPointTranslated(node.getPoint());
                node.getOption(modeSet).ifPresent(option -> {
                    final TrainNumber number =
                            option.getEntry(PathEntryType.TRAINNUMBER).orElse(TrainNumber.DEFAULT);
                    final ModeIdentifier modeIdent = new ModeIdentifier(translated, modeSet);
                    if (number.trainNumber.isEmpty()) {
                        rendering.removeTrainNumber(modeIdent);
                    } else {
                        rendering.putTrainNumber(modeIdent, number.trainNumber);
                    }
                });
            });
        } else if (entryType.equals(PathEntryType.PATHUSAGE)) {
            node.toPathIdentifier().forEach(ident -> {
                node.getOption(ident.getMode()).ifPresent(poe -> {
                    rendering.setColor(node.getPoint(), ident.getMode(),
                            poe.getEntry(PathEntryType.PATHUSAGE)
                                    .orElseGet(() -> EnumPathUsage.FREE)
                                    .getColor(getProfile().getOperationModeSettings()));
                });
            });
        }
    }

    @Override
    public void handleUIProfileUpdate() {
        initRendering();
    }

    public void setRenderPoints(final Point start, final Point end) {
        this.renderStart = start;
        this.renderEnd = end;
    }

    public void loadFromItem(final int monitorSizeX, final int monitorSizeY) {
        this.monitorSizeX = monitorSizeX;
        this.monitorSizeY = monitorSizeY;
    }

    @Override
    public SignalBoxGrid getGrid() {
        return grid;
    }

    public BlockPos getLinkedSignalBox() {
        return linkedSignalBox;
    }

    public int getMonitorSizeX() {
        return monitorSizeX;
    }

    public int getMonitorSizeY() {
        return monitorSizeY;
    }

    public Point getRenderStart() {
        return renderStart;
    }

    public Point getRenderEnd() {
        return renderEnd;
    }

    public UISignalBoxProfile getProfile() {
        return grid.getUIProfile();
    }

    @Override
    public StateInfo getStateInfo() {
        return new StateInfo(level, linkedSignalBox);
    }

    @Override
    public boolean isClientSide() {
        return level != null ? level.isClientSide : false;
    }

}