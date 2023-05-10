package com.troblecodings.signals.init;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class OSTabs {

    private OSTabs() {
    }

    public static final ItemGroup TAB = new ItemGroup("Open Signals") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(OSItems.LINKING_TOOL);
        }
    };
}