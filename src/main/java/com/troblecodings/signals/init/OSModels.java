package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    @SubscribeEvent
    public static void register(final ModelRegistryEvent event) {
        OSItems.registeredItems.forEach(OSModels::registerModel);
        CustomModelLoader.INSTANCE.onResourceManagerReload(null);
        return;
    }

    @SubscribeEvent
    public static void registerReload(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CustomModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addColor(final ColorHandlerEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        OSBlocks.BLOCKS_TO_REGISTER.forEach(block -> {
            if (block instanceof Signal) {
                final Signal signal = (Signal) block;
                if (signal.hasCostumColor())
                    colors.register((_u1, _u2, _u3, index) -> signal.colorMultiplier(index), block);
            }
        });
    }

    private static void registerModel(final Item item) {
    }
}
