package com.troblecodings.signals.init;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSSounds {

    private OSSounds() {
    }

    public static final Map<String, SoundEvent> SOUNDS = new HashMap<>();

    public static void init() {
        OpenSignalsMain.contentPacks.getFiles("sounds").forEach(entry -> {
            final String soundName = entry.getKey().toLowerCase().replace(".ogg", "");
            final ResourceLocation resource = new ResourceLocation(OpenSignalsMain.MODID,
                    soundName);
            final SoundEvent sound = new SoundEvent(resource).setRegistryName(soundName);
            SOUNDS.put(soundName, sound);
        });
    }

    @SubscribeEvent
    public static void soundRegistry(final RegistryEvent.Register<SoundEvent> event) {
        final IForgeRegistry<SoundEvent> registry = event.getRegistry();
        SOUNDS.forEach((_u, sound) -> registry.register(sound));
    }
}
