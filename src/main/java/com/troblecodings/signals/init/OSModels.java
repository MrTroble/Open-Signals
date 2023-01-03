package com.troblecodings.signals.init;

import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.models.CustomModelLoader;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class OSModels {

    private OSModels() {
    }

    @SubscribeEvent
    public static void register(final ModelRegistryEvent event) {
        OSItems.registeredItems.forEach(OSModels::registerModel);
        CustomModelLoader.INSTANCE.prepare();
        return;
    }

    @SubscribeEvent
    public static void registerReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(CustomModelLoader.INSTANCE);
    }
    
    @SubscribeEvent
    public static void reg(ModelBakeEvent event) {
        CustomModelLoader.INSTANCE.register(event);
        Map<ResourceLocation, BakedModel> model = event.getModelRegistry();
        BakedModel mod = model.get(new ModelResourceLocation(OpenSignalsMain.MODID, "hvsignal", "angel=angel0"));
        System.out.println(mod);
        return;
    }

    @SubscribeEvent
    public static void addColor(final ColorHandlerEvent.Block event) {
        final BlockColors colors = event.getBlockColors();
        OSBlocks.blocksToRegister.forEach(block -> {
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
