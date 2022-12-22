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
import com.troblecodings.signals.properties.ConfigProperty;
import com.troblecodings.signals.properties.SignalPair;
import com.troblecodings.signals.utils.FileReader;

public class ChangeConfigParser {

    private String currentSignal;
    private String nextSignal;
    private Map<String, String> savedPredicates;
    private Map<String, List<String>> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public String getNextSignal() {
        return nextSignal;
    }

    public Map<String, String> getSavedPredicates() {
        return savedPredicates;
    }

    public Map<String, List<String>> getValuesToChange() {
        return values;
    }

    public static final transient Map<SignalPair, List<ConfigProperty>> CHANGECONFIGS = new HashMap<>();

    private static final transient Gson GSON = new Gson();

    public static void loadInternConfigs() {
        loadChangeConfigs("/assets/girsignals/signalconfigs/change");
    }

    @SuppressWarnings("rawtypes")
    public static void loadChangeConfigs(final String directory) {

        for (Map.Entry<String, String> files : FileReader.readallFilesfromDierectory(directory)
                .entrySet()) {
            final ChangeConfigParser parser = GSON.fromJson(files.getValue(),
                    ChangeConfigParser.class);

            final Signal start = Signal.SIGNALS.get(parser.getCurrentSignal().toLowerCase());
            final Signal end = Signal.SIGNALS.get(parser.getNextSignal().toLowerCase());
            if (start == null || end == null) {
                OpenSignalsMain.getLogger().warn("The signal '" + parser.getCurrentSignal()
                        + "' or the signal '" + parser.getNextSignal() + "' doen't exists! "
                        + "This config with filename '" + files.getKey() + "' will be skiped!");
                continue;
            }
            final SignalPair pair = new SignalPair(start, end);
            if (CHANGECONFIGS.containsKey(pair)) {
                throw new LogicalParserException(
                        "A signalconfig with the signals [" + start.getSignalTypeName() + ", "
                                + end.getSignalTypeName() + "] does alredy exists! '"
                                + files.getKey() + "' tried to register the same pairconfig!");
            }
            final FunctionParsingInfo startInfo = new FunctionParsingInfo(start);
            final FunctionParsingInfo endInfo = new FunctionParsingInfo(
                    LogicParser.UNIVERSAL_TRANSLATION_TABLE, end);
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
                    predicate = LogicParser.predicate(valueToParse, endInfo);
                }

                final Map<SEProperty, String> values = new HashMap<>();

                for (final String value : entry.getValue()) {

                    final String[] valuetoChange = value.split("\\.");
                    final SEProperty property = (SEProperty) startInfo
                            .getProperty(valuetoChange[0]);
                    values.put(property, valuetoChange[1]);
                }

                properties.add(new ConfigProperty(predicate, values));

            }
            CHANGECONFIGS.put(pair, properties);
        }
    }
}