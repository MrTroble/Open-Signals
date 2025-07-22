package com.troblecodings.signals.core;

import net.minecraft.client.Minecraft;

public class FPSHelper {

    @SuppressWarnings("resource")
    public static int getFPS() {
        String[] fpsString = Minecraft.getInstance().fpsString.split(" fps");
        int i = Integer.valueOf(fpsString[0]);
        return i;
    }
}
