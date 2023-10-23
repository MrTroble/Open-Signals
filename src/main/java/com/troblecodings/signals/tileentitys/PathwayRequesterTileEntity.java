package com.troblecodings.signals.tileentitys;

import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class PathwayRequesterTileEntity extends SyncableTileEntity
        implements ILinkableTile, IChunkLoadable {

    private BlockPos linkedSignalBox;
    private Map.Entry<Point, Point> pathway = Maps.immutableEntry(new Point(), new Point());

    public PathwayRequesterTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private static final String LINKED_SIGNALBOX = "linkedSignalBox";
    private static final String START_POINT = "startPoint";
    private static final String END_POINT = "endPoint";

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedSignalBox = wrapper.getBlockPos(LINKED_SIGNALBOX);
        final Point start = new Point();
        start.read(wrapper.getWrapper(START_POINT));
        final Point end = new Point();
        end.read(wrapper.getWrapper(END_POINT));
        pathway = Maps.immutableEntry(start, end);
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
    }

    public void requestPathway() {
        final PosIdentifier identifier = new PosIdentifier(linkedSignalBox, level);
        if (SignalBoxHandler.getNodeFromGrid(identifier, pathway.getValue())
                .containsOutConnection()) {
            SignalBoxHandler.requesetInterSignalBoxPathway(identifier, pathway.getKey(),
                    pathway.getValue());
        } else {
            if (!SignalBoxHandler.requestPathway(identifier, pathway.getKey(),
                    pathway.getValue())) {
                SignalBoxHandler.addNextPathway(identifier, pathway.getKey(), pathway.getValue());
            }
        }
    }

    public void setNextPathway(final Point start, final Point end) {
        pathway = Maps.immutableEntry(start, end);
    }

    @Override
    public boolean link(final BlockPos pos, final CompoundTag tag) {
        @SuppressWarnings("deprecation")
        final Block block = Registry.BLOCK.get(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toShortString())));
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

}