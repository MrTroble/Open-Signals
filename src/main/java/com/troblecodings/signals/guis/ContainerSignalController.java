package com.troblecodings.signals.guis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalController extends ContainerBase implements UIClientSync {

    private final AtomicReference<Map<SEProperty, String>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    private boolean send = false;
    private Player player;
    private Runnable onUpdate;
    private BlockPos pos;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
        this.pos = info.pos == null ? BlockPos.ZERO : info.pos;
    }

    public Map<SEProperty, String> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        if (playerIn instanceof Player) {
            this.player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return pos;
    }
}
