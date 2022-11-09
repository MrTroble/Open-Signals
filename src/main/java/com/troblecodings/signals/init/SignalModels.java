package com.troblecodings.signals.init;

import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class SignalModels {

    private SignalModels() {
    }

    @SubscribeEvent
    public static void register(final ModelRegistryEvent event) {
        SignalItems.registeredItems.forEach(SignalModels::registerModel);
        registerModel(Item.getItemFromBlock(SignalBlocks.HV_SIGNAL_CONTROLLER));
        registerModel(Item.getItemFromBlock(SignalBlocks.POST));
        registerModel(Item.getItemFromBlock(SignalBlocks.SIGNAL_BOX));
        registerModel(Item.getItemFromBlock(SignalBlocks.REDSTONE_IN));
        registerModel(Item.getItemFromBlock(SignalBlocks.REDSTONE_OUT));
    }

    @SubscribeEvent
    public static void addColor(final ColorHandlerEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        SignalBlocks.blocksToRegister.forEach(block -> {
            if (block instanceof Signal) {
                final Signal sb = (Signal) block;
                if (sb.hasCostumColor())
                    colors.registerBlockColorHandler(sb::colorMultiplier, block);
            }
        });
    }

    private static void registerModel(final Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
