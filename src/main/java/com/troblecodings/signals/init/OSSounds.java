package com.troblecodings.signals.init;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class OSSounds {

    private OSSounds() {
    }

    public static final Map<String, SoundEvent> SOUNDS = new HashMap<>();

    public static void init() {
        OpenSignalsMain.contentPacks.getFiles("sounds").forEach(entry -> {
            final String soundName = entry.getKey().toLowerCase().replace(".ogg", "");
            final ResourceLocation resource = new ResourceLocation(OpenSignalsMain.MODID,
                    soundName);
            final SoundEvent sound = SoundEvent.createVariableRangeEvent(resource);
            SOUNDS.put(soundName, sound);
        });
    }

    @SubscribeEvent
    public static void soundRegistry(final RegisterEvent event) {
        event.register(Registries.SOUND_EVENT, holder -> SOUNDS.values()
                .forEach(sound -> holder.register(sound.getLocation(), sound)));
    }
}