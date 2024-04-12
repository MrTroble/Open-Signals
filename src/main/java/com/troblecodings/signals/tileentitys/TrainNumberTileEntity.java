package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

public class TrainNumberTileEntity extends SyncableTileEntity implements ILinkableTile {

    private static final String LINKED_SIGNALBOX = "linkedSignalBox";
    private static final String POINT_WRAPPER = "pointWrapper";

    public TrainNumberTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private BlockPos linkedSignalBox = null;
    private Point point = new Point(-1, -1);
    private TrainNumber number = TrainNumber.DEFAULT;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        final NBTWrapper pointWrapper = wrapper.getWrapper(POINT_WRAPPER);
        if (!pointWrapper.isTagNull())
            point.read(pointWrapper);
        number = TrainNumber.of(wrapper);
        linkedSignalBox = wrapper.getBlockPos(LINKED_SIGNALBOX);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        final NBTWrapper pointWrapper = new NBTWrapper();
        point.write(pointWrapper);
        number.writeTag(wrapper);
        wrapper.putWrapper(POINT_WRAPPER, pointWrapper);
        if (linkedSignalBox != null) {
            wrapper.putBlockPos(LINKED_SIGNALBOX, linkedSignalBox);
        }
    }

    public void updateTrainNumberManually() {
        if (level.isClientSide)
            return;
        updateTrainNumberViaRedstone();
        number = TrainNumber.DEFAULT;
    }

    public void updateTrainNumberViaRedstone() {
        if (level.isClientSide || number == TrainNumber.DEFAULT)
            return;
        loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerWorld) level, linkedSignalBox,
                (tile, _u) -> tile.getSignalBoxGrid().updateTrainNumber(point, number));
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

    public Point getCurrentPoint() {
        return point;
    }

    public BlockPos getLinkedSignalBox() {
        return linkedSignalBox;
    }

    public TrainNumber getTrainNumber() {
        return number;
    }

    public void setNewTrainNumber(final TrainNumber number) {
        this.number = number;
    }

    public void setNewPoint(final Point point) {
        this.point = point;
    }
}