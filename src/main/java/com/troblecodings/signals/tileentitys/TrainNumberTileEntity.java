package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.ILinkableTile;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.core.TrainNumber;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

public class TrainNumberTileEntity extends SyncableTileEntity implements ILinkableTile {

    private static final String LINKED_SIGNALBOX = "linkedSignalBox";

    public TrainNumberTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private BlockPos linkedSignalBox = null;
    private Point point = new Point();
    private TrainNumber number = TrainNumber.DEFAULT;

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        point.read(wrapper);
        number = TrainNumber.of(wrapper);
        linkedSignalBox = wrapper.getBlockPos(LINKED_SIGNALBOX);
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        point.write(wrapper);
        number.writeTag(wrapper);
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
        loadChunkAndGetTile(SignalBoxTileEntity.class, (ServerLevel) level, linkedSignalBox,
                (tile, _u) -> tile.getSignalBoxGrid().updateTrainNumber(point, number));
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