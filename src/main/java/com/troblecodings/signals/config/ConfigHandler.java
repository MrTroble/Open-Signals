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

        public General(final ForgeConfigSpec.Builder builder) {
            String desc;
            builder.push("General");

            desc = "Change the light emssion value of a signal."
                    + " When you change the value, the signal block lights "
                    + "up and illuminates the environment. Default: 1";
            lightEmission = builder.comment(desc).defineInRange("Signal light emission", 1, 0, 15);

            builder.pop();
        }

    }

    public static class Client {

        public final ConfigValue<Integer> signalboxBackgroundColor;
        public final ConfigValue<Integer> signalboxFreeColor;
        public final ConfigValue<Integer> signalboxSelectColor;
        public final ConfigValue<Integer> signalboxUsedColor;
        public final ConfigValue<Integer> signalboxPreparedColor;
        public final ConfigValue<Integer> signalboxTrainNumberColor;
        public final ConfigValue<Integer> signalboxShuntingColor;
        public final ConfigValue<Integer> signalboxTrainnumberBackgroundColor;

        public Client(final ForgeConfigSpec.Builder builder) {
            String desc;
            builder.push("Client Only");

            desc = "Change the background color of the signalbox gui. Default: -7631989";
            signalboxBackgroundColor = builder.comment(desc).define("Signalbox background color",
                    0xFF8B8B8B);

            desc = "Change the color of normal path elements. Default: -16777216";
            signalboxFreeColor = builder.comment(desc).define("Signalbox free color", 0xFF000000);

            desc = "Change the color of a selected path. Default: -16711936";
            signalboxSelectColor = builder.comment(desc).define("Signalbox select color",
                    0xFF00FF00);

            desc = "Change the color of a blocked path. Default: -65536";
            signalboxUsedColor = builder.comment(desc).define("Signalbox used color", 0xFFFF0000);

            desc = "Change the color of a prepared path. Default: 16776960";
            signalboxPreparedColor = builder.comment(desc).define("Signalbox prepared color",
                    0xffff00);

            desc = "Change the color of a selected shunting path. Default: -16711936";
            signalboxShuntingColor = builder.comment(desc).define("Signalbox shunting color",
                    0xFF00FF00);

            desc = "Change the color of trainnumber in the signalbox. Default: -65536";
            signalboxTrainNumberColor = builder.comment(desc)
                    .define("Signalbox trainnumber text color", 0xFFFF0000);

            desc = "Change the background color of trainnumber in the signalbox. Default: -11534336";
            signalboxTrainnumberBackgroundColor = builder.comment(desc)
                    .define("Signalbox trainnumber background color", 0xFF500000);

            desc = "Change the color of a default text. Default: -16777216";
            GuiConfigHandler.basicTextColor = builder.comment(desc).define("Basic text color",
                    0xFF000000);

            desc = "Change the color of an info text. Default: -16777046";
            GuiConfigHandler.infoTextColor = builder.comment(desc).define("info text color",
                    0xFF0000AA);

            desc = "Change the color of an error text. Default: -16776961";
            GuiConfigHandler.errorTextColor = builder.comment(desc).define("Error text color",
                    0xFF0000FF);

            builder.pop();

        }

    }

}