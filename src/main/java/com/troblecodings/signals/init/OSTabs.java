package com.troblecodings.signals.init;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class OSTabs {

    private OSTabs() {
    }

    public static final CreativeModeTab TAB = new CreativeModeTab("Open Signals") {

        @Override
        public ItemStack makeIcon() {
            for (final Placementtool tool : OSItems.placementtools) {
                if (tool.getRegistryName().toString().replace(OpenSignalsMain.MODID + ":", "")
                        .equalsIgnoreCase("placementool")) {
                    return new ItemStack(tool);
                }
            }
            throw new IllegalArgumentException();
        }
    };
}