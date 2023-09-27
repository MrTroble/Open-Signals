package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;

public class OneSignalNonPredicateConfigParser {

    private String currentSignal;
    private List<String> values;

    public static final Map<Signal, List<ConfigProperty>> SHUNTINGCONFIGS = new HashMap<>();

    public static final Map<Signal, List<ConfigProperty>> RESETCONFIGS = new HashMap<>();

    private static final Gson GSON = new Gson();

    public static void loadOneSignalConfigs() {
        loadResetConfigs();
        loadOneSignalNonPredicateConfig(SHUNTINGCONFIGS, "signalconfigs/shunting");
    }

    private static void loadResetConfigs() {
        final List<Map.Entry<String, String>> list = OpenSignalsMain.contentPacks
                .getFiles("signalconfigs/reset");
        list.forEach(entry -> {
            try {
                OneSignalPredicateConfigParser.loadOneSignalPredicateConfigs(RESETCONFIGS, entry);
            } catch (final Exception e) {
                OpenSignalsMain.getLogger()
                        .error("Reset Config '" + entry.getKey() + "' is still in old "
                                + "ResetConfig system. Please update with Predicate system!");
                loadOneSignalNonPredicateConfig(RESETCONFIGS, entry);
            }
        });
    }

    public static void loadOneSignalNonPredicateConfig(final Map<Signal, List<ConfigProperty>> map,
            final String internal) {
        final List<Map.Entry<String, String>> list = OpenSignalsMain.contentPacks
                .getFiles(internal);
        list.forEach(entry -> loadOneSignalNonPredicateConfig(map, entry));
    }

    public static void loadOneSignalNonPredicateConfig(final Map<Signal, List<ConfigProperty>> map,
            final Map.Entry<String, String> files) {
        final OneSignalNonPredicateConfigParser parser = GSON.fromJson(files.getValue(),
                OneSignalNonPredicateConfigParser.class);

        final Signal signal = checkSignal(parser.currentSignal, files.getKey());
        if (signal == null)
            return;

        if (map.containsKey(signal)) {
            throw new LogicalParserException("A signalconfig with the signals ["
                    + signal.getSignalTypeName() + "] does alredy exists! '" + files.getKey()
                    + "' tried to register the same signalconfig!");
        }

        final FunctionParsingInfo info = new FunctionParsingInfo(signal);
        final List<ConfigProperty> propertes = new ArrayList<>();
        for (final String property : parser.values) {
            final String[] value = property.split("\\.");
            propertes.add(new ConfigProperty(t -> true,
                    ImmutableMap.of((SEProperty) info.getProperty(value[0]), value[1])));
        }
        map.put(signal, propertes);
    }

    private static Signal checkSignal(final String signalName, final String filename) {
        final Signal signal = Signal.SIGNALS.get(signalName.toLowerCase());
        if (signal == null) {
            throw new ContentPackException(
                    "The signal '" + signalName + "' doesn't exists! " + "Please check " + filename
                            + " where to problem is! Valid Signals: " + Signal.SIGNALS.keySet());
        }
        return signal;
    }
}