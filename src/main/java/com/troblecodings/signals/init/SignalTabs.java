package com.troblecodings.signals.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class SignalTabs {

    private SignalTabs() {
    }

    public static final CreativeTabs TAB = new CreativeTabs("Open Signals") {

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(SignaIItems.PLACEMENT_TOOL);
        }
    };

}
