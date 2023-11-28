package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    @SubscribeEvent
    public static void register(final ModelRegistryEvent event) {
        OSItems.registeredItems.forEach(OSModels::registerModel);
        registerModel(Item.getItemFromBlock(OSBlocks.HV_SIGNAL_CONTROLLER));
        registerModel(Item.getItemFromBlock(OSBlocks.POST));
        registerModel(Item.getItemFromBlock(OSBlocks.SIGNAL_BOX));
        registerModel(Item.getItemFromBlock(OSBlocks.REDSTONE_IN));
        registerModel(Item.getItemFromBlock(OSBlocks.REDSTONE_OUT));
        registerModel(Item.getItemFromBlock(OSBlocks.COMBI_REDSTONE_INPUT));
        registerModel(Item.getItemFromBlock(OSBlocks.PATHWAY_REQUESTER));
    }

    @SubscribeEvent
    public static void addColor(final ColorHandlerEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        OSBlocks.BLOCKS_TO_REGISTER.forEach(block -> {
            if (block instanceof Signal) {
                final Signal signal = (Signal) block;
                if (signal.hasCostumColor())
                    colors.registerBlockColorHandler(signal::colorMultiplier, block);
            }
        });
    }

    private static void registerModel(final Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
