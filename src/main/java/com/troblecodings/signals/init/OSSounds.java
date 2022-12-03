package com.troblecodings.signals.init;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.utils.FileReader;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class OSSounds {

    private OSSounds() {
    }

    public static final List<SoundEvent> SOUNDS = new LinkedList<>();

    public static final Map<String, SoundEvent> SOUNDS_IN_MAP = new HashMap<>();

    public static SoundEvent andreascross;
    public static SoundEvent rottenwarn;

    public static void init() {
        andreascross = registerSound("andreas_cross");
        rottenwarn = registerSound("rottenwarn");
        loadSoundsfromJson();
    }

    public static void loadSoundsfromJson() {
        final Map<String, String> map = FileReader
                .readallFilesfromDierectory("assets/girsignals/sounds");
        map.forEach((name, _u) -> SOUNDS_IN_MAP.put(name.toLowerCase(),
                registerSound(name.toLowerCase())));
    }

    private static SoundEvent registerSound(final String soundName) {
        final ResourceLocation resource = new ResourceLocation(OpenSignalsMain.MODID, soundName);
        final SoundEvent sound = new SoundEvent(resource).setRegistryName(soundName);
        SOUNDS.add(sound);
        return sound;
    }

    @SubscribeEvent
    public static void soundRegistry(final RegistryEvent.Register<SoundEvent> event) {
        SOUNDS.forEach(sound -> event.getRegistry().register(sound));
    }
}
