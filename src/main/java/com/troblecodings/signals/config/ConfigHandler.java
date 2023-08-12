package com.troblecodings.signals.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ConfigHandler {

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final Client CLIENT = new Client(CLIENT_BUILDER);

    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    public static class Client {

        public final ConfigValue<Integer> color;

        public Client(final ForgeConfigSpec.Builder builder) {
            String desc;
            builder.push("Client");

            desc = "Description";
            color = builder.comment(desc).define("Color", 0);
            
            builder.pop();

        }

    }

}
