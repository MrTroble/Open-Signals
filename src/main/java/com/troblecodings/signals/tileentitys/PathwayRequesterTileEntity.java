package com.troblecodings.signals.tileentitys;

import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PathwayRequesterTileEntity extends SyncableTileEntity
        implements ILinkableTile, IChunkLoadable {

    private BlockPos linkedSignalBox;
    private Map.Entry<Point, Point> pathway = Maps.immutableEntry(new Point(-1, -1),
            new Point(-1, -1));
    private boolean addPWToSaver = true;

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
        loadChunkAndGetTile(SignalBoxTileEntity.class, world, linkedSignalBox, (tile, _u) -> {
            final StateInfo identifier = new StateInfo(world, linkedSignalBox);
            final SignalBoxGrid grid = tile.getSignalBoxGrid();
            if (grid.getNode(pathway.getValue()).containsOutConnection()) {
                SignalBoxHandler.requesetInterSignalBoxPathway(identifier, pathway.getKey(),
                        pathway.getValue());
            } else {
                if (!grid.requestWay(pathway.getKey(), pathway.getValue()) && addPWToSaver) {
                    grid.addNextPathway(pathway.getKey(), pathway.getValue());
                }
            }
        });

    }

    public void setAddPWToSaver(final boolean addPWToSaver) {
        this.addPWToSaver = addPWToSaver;
    }

    public void setNextPathway(final Point start, final Point end) {
        pathway = Maps.immutableEntry(start, end);
    }

    @Override
    public boolean link(final BlockPos pos, final NBTTagCompound tag) {
        final Block block = Block.REGISTRY.getObject(
                new ResourceLocation(OpenSignalsMain.MODID, tag.getString(pos.toString())));
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

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState,
            final IBlockState newSate) {
        return false;
    }

}