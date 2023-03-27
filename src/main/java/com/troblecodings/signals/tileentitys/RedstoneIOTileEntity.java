package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.core.RedstonePacket;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class RedstoneIOTileEntity extends SyncableTileEntity implements ISyncable {

    public RedstoneIOTileEntity(final TileEntityInfo info) {
        super(info);
    }

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    @Override
    public String getNameWrapper() {
        if (hasCustomName())
            return customName;
        return this.getBlockState().getBlock().getRegistryName().getPath();
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_LIST,
                linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper).toList());
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
        SignalBoxHandler.getUnlinkedPos(worldPosition).forEach(pos -> linkedPositions.remove(pos));
        linkedPositions.addAll(SignalBoxHandler.getNewLinkedPos(worldPosition));
    }

    public void sendToAll() {
        if (level.isClientSide)
            return;
        final boolean power = this.level.getBlockState(this.worldPosition)
                .getValue(RedstoneIO.POWER);
        linkedPositions.forEach(pos -> SignalBoxHandler.updateInput(pos,
                new RedstonePacket(level, worldPosition, power)));
    }

    public void link(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
        this.syncClient();
    }

    public void unlink(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
        this.syncClient();
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }
}
