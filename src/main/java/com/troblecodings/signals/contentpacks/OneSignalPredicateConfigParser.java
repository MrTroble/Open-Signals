package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;

public class OneSignalPredicateConfigParser {

    private String currentSignal;
    private Map<String, String> savedPredicates;
    private Map<String, List<String>> values;

    public static final Map<Signal, List<ConfigProperty>> DEFAULTCONFIGS = new HashMap<>();

    public static final Map<Signal, List<ConfigProperty>> DISABLECONFIGS = new HashMap<>();

    private static final Gson GSON = new Gson();

    public static void loadAllOneSignalPredicateConfigs() {
        loadOneSignalPredicateConfig(DEFAULTCONFIGS, "signalconfigs/default");
        loadOneSignalPredicateConfig(DISABLECONFIGS, "signalconfigs/disable");
    }

    private static void loadOneSignalPredicateConfig(final Map<Signal, List<ConfigProperty>> map,
            final String internal) {
        final List<Map.Entry<String, String>> list = OpenSignalsMain.contentPacks
                .getFiles(internal);
        list.forEach(entry -> loadOneSignalPredicateConfigEntry(map, entry));
    }

    public static void loadOneSignalPredicateConfigEntry(
            final Map<Signal, List<ConfigProperty>> map, final Map.Entry<String, String> files) {
        final OneSignalPredicateConfigParser parser = GSON.fromJson(files.getValue(),
                OneSignalPredicateConfigParser.class);

        final Signal signal = Signal.SIGNALS.get(parser.currentSignal.toLowerCase());
        if (signal == null) {
            OpenSignalsMain.getLogger()
                    .warn("The signal '" + parser.currentSignal + "' doesn't exists! "
                            + "This config with the filename '" + files.getKey()
                            + "' will be skiped!");
            return;
        }
        if (map.containsKey(signal)) {
            throw new LogicalParserException("A signalconfig with the signals ["
                    + signal.getSignalTypeName() + "] does alredy exists! '" + files.getKey()
                    + "' tried to register a defaultconfig for the same signal!");
        }
        final FunctionParsingInfo info = new FunctionParsingInfo(
                LogicParser.UNIVERSAL_TRANSLATION_TABLE, signal);
        final List<ConfigProperty> properties = new ArrayList<>();

        final Map<String, String> savedPredicates = parser.savedPredicates;

        for (final Map.Entry<String, List<String>> entry : parser.values.entrySet()) {

            String valueToParse = entry.getKey().toLowerCase();
            Predicate<Map<Class<?>, Object>> predicate = t -> true;

            if (valueToParse.contains("map(") && savedPredicates != null
                    && !savedPredicates.isEmpty()) {
                final char[] chars = entry.getKey().toCharArray();
                String names = "";
                boolean readKey = false;
                final StringBuilder builder = new StringBuilder();
                String mapKey = "";
                for (final char letter : chars) {

                    final String current = builder.append(letter).toString();
                    final boolean isOpenBracket = current.equals("(");
                    final boolean isCloseBracket = current.equals(")");
                    builder.setLength(0);

                    if (readKey) {
                        try {
                            if (isCloseBracket) {
                                valueToParse = valueToParse.replace("map(" + mapKey + ")",
                                        "(" + savedPredicates.get(mapKey).toLowerCase() + ")");
                                names = "";
                                mapKey = "";
                                readKey = false;
                                continue;
                            }
                            mapKey += current;
                            continue;
                        } catch (final Exception e) {
                            OpenSignalsMain.exitMinecraftWithMessage(
                                    "Something went wrong with the predicate saver in "
                                            + files.getKey() + "! Did you used it correctly?");
                        }
                    }
                    if (current.equals("(") && names.equals("map")) {
                        readKey = true;
                        mapKey = "";
                        continue;
                    }
                    final boolean isBracket = isCloseBracket || isOpenBracket;
                    if (Character.isWhitespace(letter) || current.equals("!") || isBracket) {
                        names = "";
                        mapKey = "";
                        continue;
                    }
                    names += current;
                }
            }

            if (valueToParse != null && !valueToParse.isEmpty()
                    && !valueToParse.equalsIgnoreCase("true")) {
                predicate = LogicParser.predicate(valueToParse, info);
            }

            final Map<SEProperty, String> values = new HashMap<>();

            for (final String value : entry.getValue()) {

                final String[] valuetoChange = value.split("\\.");
                final SEProperty property = (SEProperty) info.getProperty(valuetoChange[0]);
                values.put(property, valuetoChange[1]);
            }

            properties.add(new ConfigProperty(predicate, values));

        }
        map.put(signal, properties);
    }
}