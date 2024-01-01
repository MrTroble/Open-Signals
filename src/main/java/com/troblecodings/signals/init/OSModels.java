package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    @SubscribeEvent
    public static void register(final ModelEvent.RegisterAdditional event) {
        OSItems.registeredItems.forEach(OSModels::registerModel);
        CustomModelLoader.INSTANCE.onResourceManagerReload(null);
        return;
    }

    @SubscribeEvent
    public static void registerReload(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CustomModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addColor(final RegisterColorHandlersEvent.Block event) {
        OSBlocks.BLOCKS_TO_REGISTER.forEach(block -> {
            if (!(block instanceof Signal))
                return;
            event.register((_u1, _u2, _u3, index) -> ((Signal) block).colorMultiplier(index),
                    block);
        });
    }

    private static void registerModel(final Item item) {
    }
}