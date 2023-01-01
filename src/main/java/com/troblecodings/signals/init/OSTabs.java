package com.troblecodings.signals.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class OSTabs {

    private OSTabs() {
    }

    public static final CreativeModeTab TAB = new CreativeModeTab("Open Signals") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(OSItems.LINKING_TOOL);
        }
    };
}