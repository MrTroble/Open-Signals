package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.GIRSignalsMain;
import eu.gir.girsignals.blocks.ConfigProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;

public class OneSignalConfigParser {

    private String currentSignal;
    private List<String> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public List<String> getValues() {
        return values;
    }

    public static final transient Map<Signal, List<ConfigProperty>> SHUNTINGCONFIGS = new HashMap<>();

    public static final transient Map<Signal, List<ConfigProperty>> DEFAULTCONFIGS = new HashMap<>();

    public static final transient Map<Signal, List<ConfigProperty>> RESETCONFIGS = new HashMap<>();

    private static final transient Gson GSON = new Gson();

    public static void loadInternConfigs() {
        loadShuntigConfigs("/assets/girsignals/signalconfigs/shunting");
        loadDefaultConfigs("/assets/girsignals/signalconfigs/default");
        loadResetConfigs("/assets/girsignals/signalconfigs/reset");
    }

    @SuppressWarnings("rawtypes")
    public static void loadShuntigConfigs(final String directory) {
        for (Map.Entry<String, String> files : GIRFileReader.readallFilesfromDierectory(directory)
                .entrySet()) {
            final OneSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    OneSignalConfigParser.class);

            final Signal signal = checkSignal(parser.getCurrentSignal(), files.getKey());
            if (signal == null)
                continue;

            final FunctionParsingInfo info = new FunctionParsingInfo(signal);
            final List<ConfigProperty> propertes = new ArrayList<>();
            for (final String property : parser.getValues()) {
                final String[] value = property.split("\\.");
                propertes
                        .add(new ConfigProperty((SEProperty) info.getProperty(value[0]), value[1]));
            }
            SHUNTINGCONFIGS.put(signal, propertes);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void loadDefaultConfigs(final String directory) {
        for (Map.Entry<String, String> files : GIRFileReader.readallFilesfromDierectory(directory)
                .entrySet()) {
            final OneSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    OneSignalConfigParser.class);

            final Signal signal = checkSignal(parser.getCurrentSignal(), files.getKey());
            if (signal == null)
                continue;

            final FunctionParsingInfo info = new FunctionParsingInfo(signal);
            final List<ConfigProperty> propertes = new ArrayList<>();
            for (final String property : parser.getValues()) {
                final String[] value = property.split("\\.");
                propertes
                        .add(new ConfigProperty((SEProperty) info.getProperty(value[0]), value[1]));
            }
            DEFAULTCONFIGS.put(signal, propertes);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void loadResetConfigs(final String directory) {
        for (Map.Entry<String, String> files : GIRFileReader.readallFilesfromDierectory(directory)
                .entrySet()) {
            final OneSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    OneSignalConfigParser.class);

            final Signal signal = checkSignal(parser.getCurrentSignal(), files.getKey());
            if (signal == null)
                continue;

            final FunctionParsingInfo info = new FunctionParsingInfo(signal);
            final List<ConfigProperty> propertes = new ArrayList<>();
            for (final String property : parser.getValues()) {
                final String[] value = property.split("\\.");
                propertes
                        .add(new ConfigProperty((SEProperty) info.getProperty(value[0]), value[1]));
            }
            RESETCONFIGS.put(signal, propertes);
        }
    }

    private static Signal checkSignal(final String signalName, final String filename) {
        final Signal signal = Signal.SIGNALS.get(signalName.toLowerCase());
        if (signal == null) {
            GIRSignalsMain.getLogger().warn("The signal '" + signalName + "' doesn't exists! "
                    + "This config with the filename '" + filename + "' will be skiped!");
            return null;
        }
        return signal;
    }

}
