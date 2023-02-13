package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.properties.ConfigProperty;

public class OneSignalConfigParser {

    protected String currentSignal;
    protected List<String> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public List<String> getValues() {
        return values;
    }

    public static final transient Map<Signal, List<ConfigProperty>> SHUNTINGCONFIGS = new HashMap<>();

    public static final transient Map<Signal, List<ConfigProperty>> RESETCONFIGS = new HashMap<>();

    private static final transient Gson GSON = new Gson();

    public static void loadOneSignalConfigs() {
        loadShuntigConfigs();
        loadResetConfigs();
    }

    public static void loadShuntigConfigs() {
        for (final Map.Entry<String, String> files : OpenSignalsMain.contentPacks
                .getFiles("signalconfigs/shunting")) {
            final OneSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    OneSignalConfigParser.class);

            final Signal signal = checkSignal(parser.getCurrentSignal(), files.getKey());
            if (signal == null)
                continue;
            if (SHUNTINGCONFIGS.containsKey(signal)) {
                throw new LogicalParserException("A signalconfig with the signals ["
                        + signal.getSignalTypeName() + "] does alredy exists! '" + files.getKey()
                        + "' tried to register a shuntingconfig for the same signal!");
            }

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

    public static void loadResetConfigs() {
        for (final Map.Entry<String, String> files : OpenSignalsMain.contentPacks
                .getFiles("signalconfigs/reset")) {
            final OneSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    OneSignalConfigParser.class);

            final Signal signal = checkSignal(parser.getCurrentSignal(), files.getKey());
            if (signal == null)
                continue;

            if (RESETCONFIGS.containsKey(signal)) {
                throw new LogicalParserException("A signalconfig with the signals ["
                        + signal.getSignalTypeName() + "] does alredy exists! '" + files.getKey()
                        + "' tried to register the same signalconfig!");
            }

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
            throw new ContentPackException("The signal '" + signalName + "' doesn't exists! "
                    + "Please check " + filename + " where to problem is!");
        }
        return signal;
    }

}
