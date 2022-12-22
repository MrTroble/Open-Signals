package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    @SubscribeEvent
    public static void register(final ModelRegistryEvent event) {
        OSItems.registeredItems.forEach(OSModels::registerModel);
    }

    @SubscribeEvent
    public static void addColor(final ColorHandlerEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        OSBlocks.blocksToRegister.forEach(block -> {
            if (block instanceof Signal) {
                final Signal sb = (Signal) block;
                if (sb.hasCostumColor())
                    colors.register(sb, block);
            }
        });
    }

    private static void registerModel(final Item item) {
        ModelLoaderRegistry.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
