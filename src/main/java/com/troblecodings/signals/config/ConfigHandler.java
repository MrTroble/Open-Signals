package com.troblecodings.signals.config;

import com.troblecodings.guilib.ecs.GuiConfigHandler;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public final class ConfigHandler {

    private ConfigHandler() {

    }

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();

    public static final Client CLIENT = new Client(CLIENT_BUILDER);
    public static final General GENERAL = new General(GENERAL_BUILDER);

    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
    public static final ForgeConfigSpec GENERAL_SPEC = GENERAL_BUILDER.build();

    public static class General {

        public final ConfigValue<Integer> lightEmission;
        public final ConfigValue<Boolean> debugMode;
        public final ConfigValue<Boolean> canAddRSPathToSaver;
        public final ConfigValue<Boolean> canInputBlockShuntingPath;

        public General(final ForgeConfigSpec.Builder builder) {
            String desc;
            builder.push("General");

            desc = "Change the light emssion value of a signal."
                    + " When you change the value, the signal block lights "
                    + "up and illuminates the environment. Default: 1";
            lightEmission = builder.comment(desc).defineInRange("Signal light emission", 1, 0, 15);

            desc = "Toggle debug mode.";
            debugMode = builder.comment(desc).define("Debug Mode", false);

            desc = "ShuntingPaths can be added to PathwaySaver. Default: false";
            canAddRSPathToSaver = builder.comment(desc).define("canAddRSPathToSaver", false);

            desc = "Choose wether a blocking input can prevent setting a shunting path. Default: false";
            canInputBlockShuntingPath =
                    builder.comment(desc).define("canInputBlockShuntingPath", false);

            builder.pop();
        }

    }

    public static class Client {

        public final ConfigValue<String> signalboxBackgroundColor;
        public final ConfigValue<String> signalboxFreeColor;
        public final ConfigValue<String> signalboxSelectColor;
        public final ConfigValue<String> signalboxUsedColor;
        public final ConfigValue<String> signalboxPreparedColor;
        public final ConfigValue<String> signalboxTrainNumberColor;
        public final ConfigValue<String> signalboxShuntingColor;
        public final ConfigValue<String> signalboxTrainnumberBackgroundColor;
        public final ConfigValue<Integer> renderDistance;

        public Client(final ForgeConfigSpec.Builder builder) {
            String desc;
            builder.push("Client Only");

            desc = "Change the background color of the signalbox gui. Default: 0xFF8B8B8B";
            signalboxBackgroundColor =
                    builder.comment(desc).define("Signalbox background color", "0xFF8B8B8B");

            desc = "Change the color of normal path elements. Default: 0xFF000000";
            signalboxFreeColor = builder.comment(desc).define("Signalbox free color", "0xFF000000");

            desc = "Change the color of a selected path. Default: 0xFF00FF00";
            signalboxSelectColor =
                    builder.comment(desc).define("Signalbox select color", "0xFF00FF00");

            desc = "Change the color of a blocked path. Default: 0xFFFF0000";
            signalboxUsedColor = builder.comment(desc).define("Signalbox used color", "0xFFFF0000");

            desc = "Change the color of a prepared path. Default: 0xFFFF00";
            signalboxPreparedColor =
                    builder.comment(desc).define("Signalbox prepared color", "0xffff00");

            desc = "Change the color of a selected shunting path. Default: 0xFF00FF00";
            signalboxShuntingColor =
                    builder.comment(desc).define("Signalbox shunting color", "0xFF00FF00");

            desc = "Change the color of trainnumber in the signalbox. Default: 0xFFFF0000";
            signalboxTrainNumberColor =
                    builder.comment(desc).define("Signalbox trainnumber text color", "0xFFFF0000");

            desc = "Change the background color of trainnumber in the signalbox. Default: 0xFF500000";
            signalboxTrainnumberBackgroundColor = builder.comment(desc)
                    .define("Signalbox trainnumber background color", "0xFF500000");

            desc = "Change the color of a default text. Default: 0xFF000000";
            GuiConfigHandler.basicTextColor =
                    builder.comment(desc).define("Basic text color", "0xFF000000");

            desc = "Change the color of an info text. Default: 0xFF0000AA";
            GuiConfigHandler.infoTextColor =
                    builder.comment(desc).define("info text color", "0xFF0000AA");

            desc = "Change the color of an error text. Default: 0xFF0000FF";
            GuiConfigHandler.errorTextColor =
                    builder.comment(desc).define("Error text color", "0xFF0000FF");

            desc = "Change the render distance for animated signals. Default: 128";
            renderDistance = builder.comment(desc).define("Render distance", 128);

            builder.pop();

        }

    }

}