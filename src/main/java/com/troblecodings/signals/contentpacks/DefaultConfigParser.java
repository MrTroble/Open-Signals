package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.troblecodings.properties.ConfigProperty;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.utils.FileReader;

public class DefaultConfigParser {

    private String currentSignal;
    private Map<String, String> savedPredicates;
    private Map<String, List<String>> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public Map<String, String> getSavedPredicates() {
        return savedPredicates;
    }

    public Map<String, List<String>> getValuesToChange() {
        return values;
    }

    public static transient final Map<Signal, List<ConfigProperty>> DEFAULTCONFIGS = new HashMap<>();

    private static transient final Gson GSON = new Gson();

    public static void loadInternConfigs() {
        loadDefaultConfigs("/assets/girsignals/signalconfigs/default");
    }

    @SuppressWarnings("rawtypes")
    public static void loadDefaultConfigs(final String directory) {

        for (Map.Entry<String, String> files : FileReader.readallFilesfromDierectory(directory)
                .entrySet()) {
            final DefaultConfigParser parser = GSON.fromJson(files.getValue(),
                    DefaultConfigParser.class);

            final Signal signal = Signal.SIGNALS.get(parser.getCurrentSignal().toLowerCase());
            if (signal == null) {
                OpenSignalsMain.getLogger()
                        .warn("The signal '" + parser.getCurrentSignal() + "' doesn't exists! "
                                + "This config with the filename '" + files.getKey()
                                + "' will be skiped!");
                continue;
            }
            if (DEFAULTCONFIGS.containsKey(signal)) {
                throw new LogicalParserException("A signalconfig with the signals ["
                        + signal.getSignalTypeName() + "] does alredy exists! '" + files.getKey()
                        + "' tried to register the same signalconfig!");
            }
            final FunctionParsingInfo info = new FunctionParsingInfo(
                    LogicParser.UNIVERSAL_TRANSLATION_TABLE, signal);
            final List<ConfigProperty> properties = new ArrayList<>();

            final Map<String, String> savedPredicates = parser.getSavedPredicates();

            for (Map.Entry<String, List<String>> entry : parser.getValuesToChange().entrySet()) {

                String valueToParse = entry.getKey().toLowerCase();
                Predicate predicate = t -> true;

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
                                throw new ContentPackException(
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

                final Map<SEProperty, Object> values = new HashMap<>();

                for (final String value : entry.getValue()) {

                    final String[] valuetoChange = value.split("\\.");
                    final SEProperty property = (SEProperty) info.getProperty(valuetoChange[0]);

                    Object valueToSet = valuetoChange[1];
                    if (valuetoChange[1].equalsIgnoreCase("false")
                            || valuetoChange[1].equalsIgnoreCase("true")) {
                        valueToSet = Boolean.valueOf(valuetoChange[1]);
                    }
                    values.put(property, valueToSet);
                }

                properties.add(new ConfigProperty(predicate, values));

            }
            DEFAULTCONFIGS.put(signal, properties);
        }
    }

}
