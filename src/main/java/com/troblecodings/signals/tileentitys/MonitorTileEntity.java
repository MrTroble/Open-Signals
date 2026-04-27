package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.opensignals.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.MonitorTEBlock;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.RenderAnimationInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.guis.UISignalBoxProfile;
import com.troblecodings.signals.guis.UISignalBoxRendering;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public class MonitorTileEntity extends SyncableTileEntity implements ILinkableTile {

    public static final String LINKED_SIGNAL_BOX = "linkedSignalBox";
    public static final String MONITOR_SIZE_X = "monitorSizeX";
    public static final String MONITOR_SIZE_Y = "monitorSizeY";
    public static final String RENDER_START_POINT = "renderStartPoint";
    public static final String RENDER_END_POINT = "renderEndPoint";

    private SignalBoxGrid grid = new SignalBoxGrid();
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
            loadGrid();
            return true;
        }
        return false;
    }

    @Override
    public void onLoad() {
        if (level.isClientSide)
            return;
        loadGrid();
    }

    private void loadGrid() {
        loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) level, linkedSignalBox,
                (tile, chunk) -> {
                    grid = tile.getSignalBoxGrid();
                });
    }

    public void loadRenderPoints(final Point start, final Point end, final SignalBoxGrid grid) {
        final Map<Point, SignalBoxNode> nodes = new HashMap<>();
        for (final SignalBoxNode node : grid.getNodes()) {
            final Point p = node.getPoint();
            if (start.getX() <= p.getX() && p.getX() <= end.getX() && start.getY() <= p.getY()
                    && p.getY() <= end.getY()) {
                nodes.put(new Point(p.getX() - start.getX(), p.getY() - start.getY()), node);
            }
        }
        this.renderStart = start;
        this.renderEnd = end;
        rendering = UISignalBoxRendering.createSignalBoxEntity(grid, grid.getUIProfile(),
                grid.getUIProfile().getOperationModeSettings().getUIBorderSettings(),
                (_u1, _u2, _u3) -> {
                }, nodes).rendering;
    }

    public void setRenderPoints(final Point start, final Point end) {
        this.renderStart = start;
        this.renderEnd = end;
    }

    public void loadFromItem(final int monitorSizeX, final int monitorSizeY) {
        this.monitorSizeX = monitorSizeX;
        this.monitorSizeY = monitorSizeY;
    }

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

}