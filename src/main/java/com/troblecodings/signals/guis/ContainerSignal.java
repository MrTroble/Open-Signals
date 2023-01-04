package com.troblecodings.signals.guis;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;

import net.minecraft.world.entity.player.Player;

public class ContainerSignal extends ContainerBase implements ISyncable {

    public ContainerSignal(final GuiInfo info) {
        super(info);
    }

    @Override
    public boolean isValid(final Player player) {
        // TODO Maby more things
        return true;
    }

}
