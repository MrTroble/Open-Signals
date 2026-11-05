package com.troblecodings.signals.init;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 1.20.1 turned creative tabs into a registry and removed
 * {@code Item.Properties.tab}. Tabs are static (unlike blocks and items, which
 * come from content packs), so a DeferredRegister is fine here; contents are
 * contributed lazily from the registered item and block lists.
 */
public final class OSTabs {

    private OSTabs() {
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OpenSignalsMain.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(
            OpenSignalsMain.MODID,
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.Open Signals"))
                    .icon(() -> new ItemStack(OSItems.LINKING_TOOL))
                    .displayItems((parameters, output) -> {
                        OSItems.init();
                        OSItems.registeredItems.stream()
                                .filter(item -> !OSItems.CREATIVE_TAB_ITEMS.containsKey(item))
                                .forEach(output::accept);
                        OSBlocks.BLOCKS_TO_REGISTER.stream().filter(block -> block.shouldHaveItem())
                                .forEach(output::accept);
                    }).build());

    /**
     * Items that used to sit in a vanilla tab via {@code Properties.tab(...)} are
     * added back to those tabs here so the creative menu looks the same as it did
     * on 1.18.
     */
    @SubscribeEvent
    public static void buildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        OSItems.init();
        OSItems.CREATIVE_TAB_ITEMS.forEach((item, tab) -> {
            if (event.getTabKey().equals(tab)) {
                event.accept(item);
            }
        });
    }
}
