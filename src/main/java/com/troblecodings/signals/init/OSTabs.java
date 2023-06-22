package com.troblecodings.signals.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class OSTabs {

    private OSTabs() {
    }

    public static final CreativeTabs TAB = new CreativeTabs("Open Signals") {

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(OSItems.LINKING_TOOL);
        }
    };
}