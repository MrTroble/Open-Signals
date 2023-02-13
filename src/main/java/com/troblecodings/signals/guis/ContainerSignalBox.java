package com.troblecodings.signals.guis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.GuiObserver;
import com.troblecodings.signals.core.Observer;
import com.troblecodings.signals.enums.LinkType;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalBox extends ContainerBase implements UIClientSync {

    public final static String UPDATE_SET = "update";
    public final static String SIGNAL_ID = "signal";
    public final static String POS_ID = "posid";
    public final static String SIGNAL_NAME = "signalName";

    private final AtomicReference<Map<BlockPos, LinkType>> propertiesForType = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
    private final AtomicReference<Map<BlockPos, String>> names = new AtomicReference<>();
    private Player player;
    private final SignalBoxTileEntity tile;
    private Consumer<NBTWrapper> run;
    private final boolean send = true;
    private final Observer observer;

    public ContainerSignalBox(final GuiInfo info) {
        super(info);
        this.tile = info.getTile();
        this.player = info.player;
        this.observer = new GuiObserver(this);
        this.tile.getSignalBoxGrid().addListener(observer);
    }

    public ContainerSignalBox(final GuiInfo info, final Consumer<NBTWrapper> run) {
        this(info);
        this.run = run;
    }

    @Override
    public void removed(final Player playerIn) {
        super.removed(playerIn);
        this.tile.getSignalBoxGrid().removeListener(observer);
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

    public Map<BlockPos, LinkType> getPositionForTypes() {
        return this.propertiesForType.get();
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        if (tile.isBlocked() && !tile.isValid(playerIn))
            return false;
        if (this.player == null) {
            this.player = playerIn;
            this.tile.add(this);
        }
        return true;
    }
}
