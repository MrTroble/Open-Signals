package com.troblecodings.signals.init;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.utils.FileReader;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSSounds {

    private OSSounds() {
    }

    public static final Map<String, SoundEvent> SOUNDS = new HashMap<>();

    public static SoundEvent andreascross;
    public static SoundEvent rottenwarn;

    public static void init() {
        final Map<String, String> map = FileReader
                .readallFilesfromDierectory("assets/girsignals/sounds");
        map.forEach((name, _u) -> {
            final String soundName = name.toLowerCase().replace(".ogg", "");
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
