package com.troblecodings.signals.guis;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.entity.player.PlayerMP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.property.ExtendedBlockState;

public class ContainerSignalController extends Container implements UIClientSync {

    /**
     * 
     */
    private static final long serialVersionUID = 7809917666570199392L;
    private final AtomicReference<Map<SEProperty, Object>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    private boolean send = false;
    private Player player;
    private Runnable onUpdate;

    public ContainerSignalController(final SignalControllerTileEntity tile) {
        if (!tile.loadChunkAndGetTile(SignalTileEnity.class, tile.getLevel(),
                tile.getLinkedPosition(), (t, c) -> {
                    reference.set(t.getProperties());
                    final BlockState state = c.getBlockState(t.getPos());
                    referenceBlock.set((Signal) state.getBlock());
                }))
            referenceBlock.set(null);
    }

    public ContainerSignalController(final Runnable onUpdate) {
        this.onUpdate = onUpdate;
        // GuiSyncNetwork.requestRemaining(this);
    }

    private CompoundTag writeToNBT(final CompoundTag compound) {
        final Signal state = getSignal();
        if (state != null) {
            compound.putInt("state", state.getID());
            final CompoundTag comp = new CompoundTag();
            reference.get().forEach((p, o) -> p.writeToNBT(comp, o));
            compound.put("list", comp);
        }
        return compound;
    }

    @Override
    public void detectAndSendChanges() {
        if (this.player != null && !send) {
            GuiSyncNetwork.sendToClient(writeToNBT(new CompoundTag()), this.player);
            send = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readFromNBT(final CompoundTag compound) {
        if (!compound.hasKey("state")) {
            this.referenceBlock.set(null);
            return;
        }
        referenceBlock.set(Signal.SIGNALLIST.get(compound.getInt("state")));
        final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) referenceBlock.get()
                .getBlockState();

        final CompoundTag comp = (CompoundTag) compound.get("list");
        if (comp != null) {
            final HashMap<SEProperty, Object> map = new HashMap<>();
            hVExtendedBlockState.getUnlistedProperties().forEach(p -> ((SEProperty) p)
                    .readFromNBT(comp).ifPresent(obj -> map.put(((SEProperty) p), obj)));
            reference.set(map);
        }
        this.onUpdate.run();
    }

    public Map<SEProperty, Object> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
    }

    @Override
    public boolean canInteractWith(final Player playerIn) {
        if (playerIn instanceof Player) {
            this.player = (Player) playerIn;
        }
        return true;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
