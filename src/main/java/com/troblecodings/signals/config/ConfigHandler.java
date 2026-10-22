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
    @Name("Signal light emission")
    @RequiresMcRestart
    @Comment({
            "Change the light emssion value of a signal.",
            " When you change the value, the signal block lights ",
            "up and illuminates the environment. Default: 1"
    })
    public static int lightEmission = 1;

    @Name("canAddRSPathToSaver")
    @Comment("ShuntingPaths can be added to PathwaySaver. Default: false")
    public static boolean canAddRSPathToSaver = false;

    @Name("canInputBlockShuntingPath")
    @Comment("Choose wether a blocking input can prevent setting a shunting path. Default: false")
    public static boolean canInputBlockShuntingPath = false;

    @Name("Signalbox background color")
    @Comment("Change the background color of the signalbox gui. Default: 0xFF8B8B8B")
    public static String signalboxBackgroundColor = "0xFF8B8B8B";

    @Name("Signalbox free color")
    @Comment("Change the color of normal path elements. Default: 0xFF000000")
    public static String signalboxFreeColor = "0xFF000000";

    @Name("Signalbox select color")
    @Comment("Change the color of a selected path. Default: 0xFF00FF00")
    public static String signalboxSelectColor = "0xFF00FF00";

    @Name("Signalbox used color")
    @Comment("Change the color of a blocked path. Default: 0xFFFF0000")
    public static String signalboxUsedColor = "0xFFFF0000";

    @Name("Signalbox prepared color")
    @Comment("Change the color of a prepared path. Default: 0xffff00")
    public static String signalboxPreparedColor = "0xffff00";

    @Name("Signalbox shunting color")
    @Comment("Change the color of a selected shunting path. Default: 0xFF00FF00")
    public static String signalboxShuntingColor = "0xFF00FF00";

    @Name("Signalbox TrainNumber color")
    @Comment("Change the color of the TrainNumber in the UI. Default: 0xFFFF0000")
    public static String signalboxTrainNumberColor = "0xFFFF0000";

    @Name("Basic text color")
    @Comment("Change the color of a default text. Default: -0xFF000000")
    public static String basicTextColor = GuiConfigHandler.basicTextColor;

    @Name("info text color")
    @Comment("Change the color of an info text. Default: -0xFF0000AA")
    public static String infoTextColor = GuiConfigHandler.infoTextColor;

    @Name("Error text color")
    @Comment("Change the color of an error text. Default: 0xFF0000FF")
    public static String errorTextColor = GuiConfigHandler.errorTextColor;

    @Name("Signalbox trainnumber text color")
    @Comment("Change the color of trainnumber in the signalbox. Default: 0xFFFF0000")
    public static String signalboxTrainnumberColor = "0xFFFF0000";

    @Name("Signalbox trainnumber background color")
    @Comment("Change the background color of trainnumber in the signalbox. Default: 0xFF500000")
    public static String signalboxTrainnumberBackgroundColor = "0xFF500000";

    @RequiresMcRestart
    @Name("Render distance")
    @Comment("Change the render distance for animated signals. Default: 128")
    public static int renderDistance = 128;

    @Name("highlight duration")
    @Comment("Change the duration of the highlight. Default: 10 (seconds)")
    public static int highlightDuration = 10;

    @Name("Debug Mode")
    @Comment("Toggle debug mode.")
    public static boolean debugMode = false;
}
