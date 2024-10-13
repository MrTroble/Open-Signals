package com.troblecodings.signals.tileentitys;

import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;
import com.troblecodings.signals.signalbox.SignalBoxUtil;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class PathwayRequesterTileEntity extends SyncableTileEntity
        implements ILinkableTile, IChunkLoadable {

    private BlockPos linkedSignalBox;
    private Map.Entry<Point, Point> pathway = Maps.immutableEntry(new Point(-1, -1),
            new Point(-1, -1));
    private boolean addPWToSaver = true;

    public PathwayRequesterTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private static final String LINKED_SIGNALBOX = "linkedSignalBox";
    private static final String START_POINT = "startPoint";
    private static final String END_POINT = "endPoint";
    private static final String ADD_TO_PW_SAVER = "addToPWSaver";

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedSignalBox = wrapper.getBlockPos(LINKED_SIGNALBOX);
        final Point start = new Point();
        start.read(wrapper.getWrapper(START_POINT));
        final Point end = new Point();
        end.read(wrapper.getWrapper(END_POINT));
        pathway = Maps.immutableEntry(start, end);
        addPWToSaver = wrapper.getBoolean(ADD_TO_PW_SAVER);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        if (linkedSignalBox != null)
            wrapper.putBlockPos(LINKED_SIGNALBOX, linkedSignalBox);
        final NBTWrapper startPoint = new NBTWrapper();
        pathway.getKey().write(startPoint);
        final NBTWrapper endPoint = new NBTWrapper();
        pathway.getValue().write(endPoint);
        wrapper.putWrapper(START_POINT, startPoint);
        wrapper.putWrapper(END_POINT, endPoint);
        wrapper.putBoolean(ADD_TO_PW_SAVER, addPWToSaver);
    }

    public void requestPathway() {
        loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerWorld) level, linkedSignalBox,
                (tile, _u) -> {
                    final SignalBoxGrid grid = tile.getSignalBoxGrid();
                    final PathType type = SignalBoxUtil.getPathTypeFrom(
                            grid.getNode(pathway.getKey()), grid.getNode(pathway.getValue()));
                    if (!type.equals(PathType.NONE)) {
                        final PathwayRequestResult result = grid.requestWay(pathway.getKey(),
                                pathway.getValue(), type);
                        if (!result.isPass() && result.canBeAddedToSaver() && addPWToSaver) {
                            grid.addNextPathway(pathway.getKey(), pathway.getValue(), type);
                        }
                    }

                });
    }

    public void setNextPathway(final Point start, final Point end) {
        pathway = Maps.immutableEntry(start, end);
    }

    public void setAddPWToSaver(final boolean addPWToSaver) {
        this.addPWToSaver = addPWToSaver;
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundNBT tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(new ResourceLocation(OpenSignalsMain.MODID,
                tag.getString(OSItems.readStringFromPos(pos))));
        if (block instanceof SignalBox) {
            linkedSignalBox = pos;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasLink() {
        return linkedSignalBox != null;
    }

    @Override
    public boolean unlink() {
        linkedSignalBox = null;
        return true;
    }

    public BlockPos getLinkedSignalBox() {
        return linkedSignalBox;
    }

    public Map.Entry<Point, Point> getNextPathway() {
        return pathway;
    }

    public boolean shouldPWBeAddedToSaver() {
        return addPWToSaver;
    }

}
