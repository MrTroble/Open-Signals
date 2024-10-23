package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.properties.PredicatedPropertyBase.ConfigProperty;

public class ChangeConfigParser {

    private String currentSignal;
    private String nextSignal;
    private Map<String, String> savedPredicates;
    private Map<String, List<String>> values;

    public static final Map<Map.Entry<Signal, Signal>, List<ConfigProperty>> CHANGECONFIGS = new HashMap<>();

    private static final Gson GSON = new Gson();

    public static void loadChangeConfigs() {
        for (final Map.Entry<String, String> files : OpenSignalsMain.contentPacks
                .getFiles("signalconfigs/change")) {
            try {
                final ChangeConfigParserV2 parser = GSON.fromJson(files.getValue(),
                        ChangeConfigParserV2.class);
                for (final String currentSignal : parser.currentSignals) {
                    for (final String nextSignal : parser.nextSignals) {
                        loadConfigForPair(files.getKey(), currentSignal, nextSignal,
                                parser.savedPredicates, parser.values);
                    }
                }
            } catch (final Exception e) {
                OpenSignalsMain.getLogger()
                        .error("Please update your change config [" + files.getKey() + "]!");
                final ChangeConfigParser parser = GSON.fromJson(files.getValue(),
                        ChangeConfigParser.class);
                loadConfigForPair(files.getKey(), parser.currentSignal, parser.nextSignal,
                        parser.savedPredicates, parser.values);
            }
        }
    }

    private static void loadConfigForPair(final String fileName, final String currentSignal,
            final String nextSignal, final Map<String, String> savedPredicates,
            final Map<String, List<String>> values) {
        try {

            final Signal start = Signal.SIGNALS.get(currentSignal.toLowerCase());
            final Signal end = Signal.SIGNALS.get(nextSignal.toLowerCase());
            if (start == null || end == null) {
                OpenSignalsMain.getLogger()
                        .warn("The signal '" + nextSignal + "' or the signal '" + nextSignal
                                + "' doen't exists! " + "This config with filename '" + fileName
                                + "' will be skiped!");
                return;
            }
            final Map.Entry<Signal, Signal> pair = Maps.immutableEntry(start, end);
            if (CHANGECONFIGS.containsKey(pair)) {
                throw new LogicalParserException(
                        "A signalconfig with the signals [" + start.getSignalTypeName() + ", "
                                + end.getSignalTypeName() + "] does alredy exists! '" + fileName
                                + "' tried to register a chaneconfig for the same signalpair!");
            }
            final FunctionParsingInfo startInfo = new FunctionParsingInfo(start);
            final FunctionParsingInfo endInfo = new FunctionParsingInfo(
                    LogicParser.UNIVERSAL_TRANSLATION_TABLE, end);
            final List<ConfigProperty> properties = new ArrayList<>();

            for (final Map.Entry<String, List<String>> entry : values.entrySet()) {

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
                                                + fileName + "! Did you used it correctly?");
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
                    predicate = LogicParser.predicate(valueToParse, endInfo);
                }

                final Map<SEProperty, String> propertiesToSet = new HashMap<>();

                for (final String value : entry.getValue()) {

                    final String[] valuetoChange = value.split("\\.");
                    final SEProperty property = (SEProperty) startInfo
                            .getProperty(valuetoChange[0]);
                    propertiesToSet.put(property, valuetoChange[1]);
                }

                properties.add(new ConfigProperty(predicate, propertiesToSet));

            }
            CHANGECONFIGS.put(pair, properties);
        } catch (final Exception e) {
            OpenSignalsMain.getLogger().error("There was a problem loading the ChangeConfig ["
                    + fileName + "]! Please check the file!");
            e.printStackTrace();
        }
    }

    private static class ChangeConfigParserV2 {

        private String[] currentSignals;
        private String[] nextSignals;
        private Map<String, String> savedPredicates;
        private Map<String, List<String>> values;

    }
}