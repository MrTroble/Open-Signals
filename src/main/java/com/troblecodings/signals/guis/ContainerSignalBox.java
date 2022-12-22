package com.troblecodings.signals.guis;

import java.awt.Container;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap.Builder;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.PlayerMP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.ILevelNameable;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalBox extends Container implements UIClientSync {

    private static final long serialVersionUID = -2076092400025320207L;
    public final static String UPDATE_SET = "update";
    public final static String SIGNAL_ID = "signal";
    public final static String POS_ID = "posid";
    public final static String SIGNAL_NAME = "signalName";

    private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, String>> names = new AtomicReference<>();
    private Player player;
    private SignalBoxTileEntity tile;
    private Consumer<CompoundTag> run;
    private boolean send = true;

    public ContainerSignalBox(final SignalBoxTileEntity tile) {
        this.tile = tile;
    }

    public ContainerSignalBox(final Consumer<CompoundTag> run) {
        this.run = run;
        // GuiSyncNetwork.requestRemaining(this);
    }

    @Override
    public void detectAndSendChanges() {
        if (this.player != null && send) {
            send = false;
            final CompoundTag compound = new CompoundTag();
            final ListTag typeList = new ListTag();
            this.tile.getPositions().keySet().forEach(pos -> {
                final CompoundTag entry = new CompoundTag();
                entry.put(POS_ID, NBTUtil.createPosTag(pos));
                tile.loadChunkAndGetTile(ILevelNameable.class, tile.getLevel(), pos,
                        (sig, _u) -> entry.setString(SIGNAL_NAME, sig.getName()));
                final Signal signal = tile.getSignal(pos);
                if (signal != null) {
                    entry.putInt(SIGNAL_ID, signal.getID());
                }
                typeList.add(entry);
            });
            compound.put(UPDATE_SET, typeList);
            GuiSyncNetwork.sendToClient(compound, getPlayer());
        }
    }

    @Override
    public boolean canInteractWith(final Player playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (playerIn instanceof PlayerMP && this.player == null) {
            this.player = (PlayerMP) playerIn;
            this.tile.add(this);
        }
        return true;
    }

    @Override
    public void readFromNBT(final CompoundTag compound) {
        if (compound.hasKey(UPDATE_SET)) {
            final ListTag update = (ListTag) compound.get(UPDATE_SET);
            final Builder<BlockPos, Signal> immutableMap = new Builder<>();
            final Builder<BlockPos, String> nameBuilder = new Builder<>();
            update.forEach(nbt -> {
                final CompoundTag comp = (CompoundTag) nbt;
                final BlockPos pos = NBTUtil.getPosFromTag(comp.getCompound(POS_ID));
                if (compound.hasKey(SIGNAL_ID)) {
                    final Signal signal = Signal.SIGNALLIST.get(compound.getInt(SIGNAL_ID));
                    immutableMap.put(pos, signal);
                }
                nameBuilder.put(pos, comp.getString(SIGNAL_NAME));
            });
            properties.set(immutableMap.build());
            names.set(nameBuilder.build());
            return;
        }
        this.run.accept(compound);
    }

    @Override
    public void onContainerClosed(final Player playerIn) {
        if (this.tile != null)
            this.tile.remove(this);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public Map<BlockPos, Signal> getProperties() {
        return this.properties.get();
    }

    public Map<BlockPos, String> getNames() {
        return this.names.get();
    }
}
