package com.troblecodings.signals.guis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.guilib.ecs.BaseContainer;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
import com.troblecodings.guilib.ecs.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalBox extends BaseContainer implements UIClientSync {

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
    	super(OpenSignalsMain.handler.getContainerInfo(SignalBox.class));
        this.tile = tile;
    }

    public ContainerSignalBox(final Consumer<CompoundTag> run) {
    	super(OpenSignalsMain.handler.getContainerInfo(SignalBox.class));
        this.run = run;
    }

    // TODO rewrite
    public void detectAndSendChanges() {
        if (this.player != null && send) {
            send = false;
            final CompoundTag compound = new CompoundTag();
            final ListTag typeList = new ListTag();
            this.tile.getPositions().keySet().forEach(pos -> {
                final CompoundTag entry = new CompoundTag();
                entry.put(POS_ID, NbtUtils.writeBlockPos(pos));
                tile.loadChunkAndGetTile(NamableWrapper.class, tile.getLevel(), pos,
                        (sig, _u) -> entry.putString(SIGNAL_NAME, sig.getNameAsStringWrapper()));
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
    public void removed(Player playerIn) {
    	super.removed(playerIn);
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

	@Override
	public boolean stillValid(Player playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.player == null) {
            this.player = playerIn;
            this.tile.add(this);
        }
		return true;
	}
}
