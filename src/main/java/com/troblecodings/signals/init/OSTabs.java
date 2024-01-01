package com.troblecodings.signals.init;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Row;

public final class OSTabs {

    private OSTabs() {
    }

    public static final CreativeModeTab TAB = CreativeModeTab.builder(Row.TOP, 1)
            .icon(() -> OSItems.LINKING_TOOL.getDefaultInstance())
            .title(Component.literal("Open Signals")).build();

}