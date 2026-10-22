package com.troblecodings.signals.enums;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.resources.ResourceLocation;

public enum SignalBoxIcons {

    SYMBOLS(new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/symbols.png"),
            SignalBoxSymbols.values().length),
    SIGNALS(new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/signals.png"), 15),
    SIGNS(new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/signs.png"),
            SignalBoxSigns.values().length);

    private final ResourceLocation loc;
    private final double width;

    private SignalBoxIcons(final ResourceLocation loc, final int width) {
        this.loc = loc;
        this.width = width;
    }

    public ResourceLocation getResourceLocation() {
        return loc;
    }

    public double getX(final int id) {
        return id / width;
    }

    public double getMX(final int id) {
        return id / width + 1 / width;
    }

    public enum SignalBoxSymbols {
        SIGNALS, REDSTONE_IN, REDSTONE_OUT, COUNTER, EMERGENCY, SAVE, REDSTONE, REDSTONE_OFF,
        REDSTONE_OFF_BLOCKED, REDSTONE_ON, REDSTONE_ON_BLOCKED;

        private SignalBoxSymbols() {
        }
    }

    public enum SignalBoxSigns {
        RA10, SH2, NE1, NE5, ZS3, ARROW, ARROW_IN, ARROW_OUT;

        private SignalBoxSigns() {
        }
    }
}
