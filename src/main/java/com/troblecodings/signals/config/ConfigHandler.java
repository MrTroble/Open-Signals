package com.troblecodings.signals.config;

import com.troblecodings.guilib.ecs.GuiConfigHandler;
import com.troblecodings.signals.OpenSignalsMain;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

@Config(modid = OpenSignalsMain.MODID)
public final class ConfigHandler {

    private ConfigHandler() {
    }

    @RangeInt(min = 0, max = 15)
    @Name(value = "Signal light emission")
    @RequiresMcRestart
    @Comment(value = {
            "Change the light emssion value of a signal.",
            " When you change the value, the signal block lights ",
            "up and illuminates the environment. Default: 1"
    })
    public static int lightEmission = 1;

    @Name(value = "Signalbox background color")
    @Comment(value = {
            "Change the background color of the signalbox gui. Default: -7631989"
    })
    public static int signalboxBackgroundColor = 0xFF8B8B8B;

    @Name(value = "Signalbox free color")
    @Comment(value = {
            "Change the color of normal path elements. Default: -16777216"
    })
    public static int signalboxFreeColor = 0xFF000000;

    @Name(value = "Signalbox select color")
    @Comment(value = {
            "Change the color of a selected path. Default: -16711936"
    })
    public static int signalboxSelectColor = 0xFF00FF00;

    @Name(value = "Signalbox used color")
    @Comment(value = {
            "Change the color of a blocked path. Default: -65536"
    })
    public static int signalboxUsedColor = 0xFFFF0000;

    @Name(value = "Basic text color")
    @Comment(value = {
            "Change the color of a default text. Default: -16777216"
    })
    public static int basicTextColor = GuiConfigHandler.basicTextColor;

    @Name(value = "info text color")
    @Comment(value = {
            "Change the color of an info text. Default: -16777046"
    })
    public static int infoTextColor = GuiConfigHandler.infoTextColor;

    @Name(value = "Error text color")
    @Comment(value = {
            "Change the color of an error text. Default: -16776961"
    })
    public static int errorTextColor = GuiConfigHandler.errorTextColor;
}
