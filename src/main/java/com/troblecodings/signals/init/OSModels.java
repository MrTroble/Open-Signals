package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.client.color.block.BlockColors;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    /**
     * Signals have no blockstate json (they are defined by content packs at runtime), so vanilla
     * baking produces missing models for them. Their real models are baked here and written over
     * those entries. This replaces the pre-1.19 approach of swapping a custom Map into
     * ForgeModelBakery.unbakedCache, which no longer exists.
     */
    @SubscribeEvent
    public static void modifyBakingResult(final ModelEvent.ModifyBakingResult event) {
        CustomModelLoader.INSTANCE.bakeInto(event.getModels(), event.getModelBakery());
    }

    @SubscribeEvent
    public static void registerReload(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CustomModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void addColor(final RegisterColorHandlersEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        OSBlocks.BLOCKS_TO_REGISTER.forEach(block -> {
            if (block instanceof Signal) {
                final Signal signal = (Signal) block;
                if (signal.hasCostumColor())
                    colors.register((_u1, _u2, _u3, index) -> signal.colorMultiplier(index), block);
            }
        });
    }
}
