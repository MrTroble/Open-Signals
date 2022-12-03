package eu.gir.girsignals.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class GIRTabs {

    private GIRTabs() {
    }

    public static final CreativeTabs TAB = new CreativeTabs("Open Signals") {

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(GIRItems.PLACEMENT_TOOL);
        }
    };

}
