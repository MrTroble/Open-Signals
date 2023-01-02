package com.troblecodings.signals.guis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.core.BaseContainer;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiHandler.GuiCreateInfo;
import com.troblecodings.guilib.ecs.GuiSyncNetwork;
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

public class ContainerSignalBox extends ContainerBase implements UIClientSync {

    public final static String UPDATE_SET = "update";
    public final static String SIGNAL_ID = "signal";
    public final static String POS_ID = "posid";
    public final static String SIGNAL_NAME = "signalName";

    private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, String>> names = new AtomicReference<>();
    private Player player;
    private SignalBoxTileEntity tile;
    private Consumer<NBTWrapper> run;
    private boolean send = true;

    public ContainerSignalBox(final GuiCreateInfo info) {
        super(info);
        this.tile = info.getTile();
    }

    public ContainerSignalBox(final GuiCreateInfo info, final Consumer<NBTWrapper> run) {
        super(info);
        this.run = run;
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
