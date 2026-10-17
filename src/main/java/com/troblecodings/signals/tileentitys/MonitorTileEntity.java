package com.troblecodings.signals.tileentitys;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.I18Wrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.MonitorTEBlock;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.MonitorBlockProperties;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.guis.UISignalBoxRendering.SelectionType;
import com.troblecodings.signals.handler.ClientMonitorNetworkHandler;
import com.troblecodings.signals.handler.MonitorNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.network.SignalBoxNetworkMode;
import com.troblecodings.signals.network.SignalBoxNetworkReader;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MonitorTileEntity extends SyncableTileEntity
        implements ILinkableTile, SignalBoxNetworkReader {

    public static final String LINKED_SIGNAL_BOX = "linkedSignalBox";
    public static final String MONITOR_SIZE_X = "monitorSizeX";
    public static final String MONITOR_SIZE_Y = "monitorSizeY";
    public static final String RENDER_START_POINT = "renderStartPoint";
    public static final String RENDER_END_POINT = "renderEndPoint";

    private static final List<SignalBoxNetworkMode> ALLOWED_NETWORK_MODES = ImmutableList.of(
            SignalBoxNetworkHandler.GRID, SignalBoxNetworkHandler.ENTRY,
            SignalBoxNetworkHandler.NODE_SPECIAL_ENTRIES, SignalBoxNetworkHandler.SUBSIDIARY);

    private final SignalBoxGrid grid = new SignalBoxGrid();
    private final Map<Point, Point> translatedPoints = new HashMap<>();
    private SignalBoxNetworkHandler network;
    private BlockPos linkedSignalBox = BlockPos.ORIGIN;
    private int monitorSizeX, monitorSizeY;
    private Point renderStart = new Point(-1, -1), renderEnd = new Point(-1, -1);
    private UISignalBoxRendering rendering;
    private Point startPoint = null;

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
        final Block block = getWorld().getBlockState(getPos()).getBlock();
        if (!(block instanceof MonitorTEBlock) || rendering == null)
            return;
        ((MonitorTEBlock) block).render(info, this, rendering);
    }

    public boolean onBlockActivated(final float hitX, final float hitY,
            final MonitorBlockProperties props, final EntityPlayer playerIn) {
        if (!hasLink() || renderStart.equals(new Point(-1, -1)))
            return false;
        final int cellsX = renderEnd.getX() - renderStart.getX() + 1;
        final int cellsY = renderEnd.getY() - renderStart.getY() + 1;

        final float insets = (props.getInsets() - 6) / MonitorTEBlock.STEPS_PER_BLOCK;
        final float mx = hitX;
        final float my = monitorSizeY - hitY;

        final float contentW = monitorSizeX - 2 * insets;
        final float contentH = monitorSizeY - 2 * insets;

        final float u = (mx - insets) / contentW;
        final float v = (my - insets) / contentH;
        final int px = clamp((int) Math.floor(u * cellsX), 0, cellsX - 1);
        final int py = clamp((int) Math.floor(v * cellsY), 0, cellsY - 1);

        final Point point = new Point(px, py);
        final SignalBoxNode node = grid.getNode(getPointRetranslated(point));
        if (node == null)
            return false;
        if (startPoint == null && node.isValidStart()) {
            startPoint = point;
            rendering.addSelection(
                    grid.getUIProfile().getOperationModeSettings().getUserSelectionColor(), point,
                    SelectionType.FIRST);
            return true;
        }
        if (!point.equals(startPoint) && node.isValidEnd()) {
            requestPathway(startPoint, point, playerIn);
            rendering.removeSelection(SelectionType.FIRST);
            startPoint = null;
            return true;
        }
        if (point.equals(startPoint)) {
            rendering.removeSelection(SelectionType.FIRST);
            startPoint = null;
        }
        return false;
    }

    private static int clamp(final int v, final int min, final int max) {
        return Math.max(min, Math.min(max, v));
    }

    private void requestPathway(final Point start, final Point end, final EntityPlayer playerIn) {
        final Point p1 = getPointRetranslated(start);
        final Point p2 = getPointRetranslated(end);
        final SignalBoxNode nodeP1 = grid.getNode(p1);
        final SignalBoxNode nodeP2 = grid.getNode(p2);
        if (nodeP1 == null || nodeP2 == null)
            return;
        final PathType pathType =
                nodeP1.getPossibleTypes(nodeP2).stream().findFirst().orElse(PathType.NONE);
        MonitorNetworkHandler.sendPathwayRequest(world, linkedSignalBox, p1, p2, pathType);
    }

    @Override
    public boolean hasLink() {
        return linkedSignalBox != null && !linkedSignalBox.equals(BlockPos.ORIGIN);
    }

    @Override
    public boolean unlink() {
        MonitorNetworkHandler.deregisterMonitorFromBox(this);
        linkedSignalBox = BlockPos.ORIGIN;
        return true;
    }

    @Override
    public boolean link(final BlockPos pos, final NBTTagCompound tag) {
        final Block block = Block.REGISTRY.getObject(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toString())));
        if (block instanceof SignalBox) {
            linkedSignalBox = pos;
            MonitorNetworkHandler.registerMonitorToBox(this);
            return true;
        }
        return false;
    }

    @Override
    public void onLoad() {
        if (world.isRemote) {
            ClientMonitorNetworkHandler.loadUpdate(this);
        }
    }

    public void loadBoxUpdate(final ReadBuffer buffer) {
        final boolean wasNetworkNull = network == null;
        if (wasNetworkNull) {
            network = new SignalBoxNetworkHandler(this, grid);
        }
        network.desirializeBuffer(buffer, ALLOWED_NETWORK_MODES);
        if (wasNetworkNull) {
            initRendering();
        }
    }

    public void loadRenderPoints(final ReadBuffer buffer) {
        linkedSignalBox = buffer.getBlockPos();
        monitorSizeX = buffer.getInt();
        monitorSizeY = buffer.getInt();
        renderStart = buffer.getINetworkSaveable(Point.class);
        renderEnd = buffer.getINetworkSaveable(Point.class);
        initRendering();
    }

    private void initRendering() {
        rendering = new UISignalBoxRendering(grid, getProfile(),
                UISignalBoxProfile.defaultProfile.getOperationModeSettings().getUIBorderSettings(),
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
            if (translated != null) {
                nodes.put(translated, node);
            }
        }
        nodes.forEach((p, n) -> {
            rendering.updateNode(p, n);
            onEntryUpdate(n, PathEntryType.TRAINNUMBER);
            onEntryUpdate(n, PathEntryType.PATHUSAGE);
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

    private Point getPointRetranslated(final Point p) {
        return new Point(p.getX() + renderStart.getX(), p.getY() + renderStart.getY());
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
    public void onEntryUpdate(final SignalBoxNode node, final PathEntryType<?> entryType) {
        final Point translated = getPointTranslated(node.getPoint());
        if (translated == null)
            return;
        if (entryType.equals(PathEntryType.TRAINNUMBER)) {
            node.iterator().forEachRemaining(modeSet -> {
                if (!(modeSet.mode == EnumGuiMode.TRAIN_NUMBER))
                    return;
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
                    rendering.setColor(translated, ident.getMode(),
                            poe.getEntry(PathEntryType.PATHUSAGE)
                                    .orElseGet(() -> EnumPathUsage.FREE)
                                    .getColor(getProfile().getOperationModeSettings()));
                });
            });
        }
    }

    @Override
    public void onNodeUpdate(final SignalBoxNode node) {
        final Point translated = getPointTranslated(node.getPoint());
        if (translated == null)
            return;
        node.forEach((mode) -> {
            final SubsidiaryState state = node.getSubsidiaryState(mode);
            if (state != null) {
                node.updateState(mode, SignalState.combine(state.getSubsidiaryShowType()));
            }
        });
        rendering.updateNode(translated, node);
    }

    @Override
    public void handleUIProfileUpdate() {
        initRendering();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleAddSavedPathway(final Point p1, final Point p2, final PathType type,
            final PathwayRequestMode result) {
        final World world = getWorld();
        if (!world.isRemote)
            return;

        final EntityPlayer player = Minecraft.getMinecraft().player;
        player.sendMessage(new TextComponentString(I18Wrapper.format("error." + result.getName())
                + " - " + I18Wrapper.format("info.pathwaysaver")));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handlePathwayRequestResponse(final PathwayRequestMode mode) {
        final World world = getWorld();
        if (!world.isRemote)
            return;

        final EntityPlayer player = Minecraft.getMinecraft().player;
        player.sendMessage(new TextComponentString(I18Wrapper.format("error." + mode.getName())));
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
        return new StateInfo(getWorld(), linkedSignalBox);
    }

    @Override
    public boolean isClientSide() {
        return world != null ? world.isRemote : false;
    }

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState,
            final IBlockState newSate) {
        return false;
    }

}