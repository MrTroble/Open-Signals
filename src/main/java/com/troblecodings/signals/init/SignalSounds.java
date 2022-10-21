package com.troblecodings.signals.init;

import java.util.LinkedList;
import java.util.List;

import com.troblecodings.signals.GirsignalsMain;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class SignalSounds {
    
    private SignalSounds() {
    }
    
    public static final List<SoundEvent> SOUNDS = new LinkedList<>();
    
    public static SoundEvent andreascross;
    
    public static void init() {
        andreascross = registerSound("andreas_cross");
    }

    private static SoundEvent registerSound(final String soundName) {
        final ResourceLocation resource = new ResourceLocation(GirsignalsMain.MODID, soundName);
        final SoundEvent sound = new SoundEvent(resource).setRegistryName(soundName);
        SOUNDS.add(sound);
        return sound;
    }
    
    public static void soundRegistry(final RegistryEvent.Register<SoundEvent> event) {
        SOUNDS.forEach(sound -> event.getRegistry().register(sound));
    }
}
