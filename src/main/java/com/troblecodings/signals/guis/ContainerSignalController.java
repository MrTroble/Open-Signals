package com.troblecodings.signals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSignalController extends Container implements UIClientSync {

    private final AtomicReference<Map<SEProperty<?>, Object>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    private boolean send = false;
    private EntityPlayerMP player;
    private Runnable onUpdate;

    public ContainerSignalController(final SignalControllerTileEntity tile) {
        if (!tile.loadChunkAndGetTile(SignalTileEnity.class, tile.getWorld(),
                tile.getLinkedPosition(), (t, c) -> {
                    reference.set(t.getProperties());
                    final IBlockState state = c.getBlockState(t.getPos());
                    referenceBlock.set((Signal) state.getBlock());
                }))
            referenceBlock.set(null);
    }

    public ContainerSignalController(final Runnable onUpdate) {
        this.onUpdate = onUpdate;
        //GuiSyncNetwork.requestRemaining(this);
    }

    private NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final Signal state = getSignal();
        if (state != null) {
            compound.setInteger("state", state.getID());
            final NBTTagCompound comp = new NBTTagCompound();
            reference.get().forEach((p, o) -> p.writeToNBT(comp, o));
            compound.setTag("list", comp);
        }
        return compound;
    }

    @Override
    public void detectAndSendChanges() {
        if (this.player != null && !send) {
            GuiSyncNetwork.sendToClient(writeToNBT(new NBTTagCompound()), this.player);
            send = true;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        if (!compound.hasKey("state")) {
            this.referenceBlock.set(null);
            return;
        }
        referenceBlock.set(Signal.SIGNALLIST.get(compound.getInteger("state")));
        final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) referenceBlock.get()
                .getBlockState();

        final NBTTagCompound comp = (NBTTagCompound) compound.getTag("list");
        if (comp != null) {
            final HashMap<SEProperty<?>, Object> map = new HashMap<>();
            hVExtendedBlockState.getUnlistedProperties().forEach(p -> ((SEProperty<?>) p)
                    .readFromNBT(comp).ifPresent(obj -> map.put(((SEProperty<?>) p), obj)));
            reference.set(map);
        }
        this.onUpdate.run();
    }

    public Map<SEProperty<?>, Object> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        if (playerIn instanceof EntityPlayerMP) {
            this.player = (EntityPlayerMP) playerIn;
        }
        return true;
    }

    @Override
    public EntityPlayerMP getPlayer() {
        return player;
    }
}
