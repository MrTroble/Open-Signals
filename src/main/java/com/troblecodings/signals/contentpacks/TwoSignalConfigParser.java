package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.models.parser.LogicalParserException;
import com.troblecodings.signals.utils.FileReader;

public class TwoSignalConfigParser {

    private String currentSignal;
    private String nextSignal;
    private List<String> savedPredicates;
    private Map<String, List<String>> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public String getNextSignal() {
        return nextSignal;
    }

    public List<String> getSavedPredicates() {
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
            final TwoSignalConfigParser parser = GSON.fromJson(files.getValue(),
                    TwoSignalConfigParser.class);

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

            final List<String> savedPredicates = parser.getSavedPredicates();

            for (Map.Entry<String, List<String>> entry : parser.getValuesToChange().entrySet()) {

                String valueToParse = entry.getKey().toLowerCase();
                Predicate predicate = t -> true;

                if (valueToParse.contains("list(") && savedPredicates != null
                        && !savedPredicates.isEmpty()) {
                    final char[] chars = entry.getKey().toCharArray();
                    String names = "";
                    boolean readInt = false;
                    final StringBuilder builder = new StringBuilder();
                    for (final char letter : chars) {

                        final String current = builder.append(letter).toString();
                        builder.setLength(0);

                        if (readInt) {
                            try {
                                final int postionFromList = Integer.parseInt(current);
                                valueToParse = valueToParse.replace("list(" + current + ")", "("
                                        + savedPredicates.get(postionFromList).toLowerCase() + ")");
                                readInt = false;
                                continue;
                            } catch (final NumberFormatException e) {
                                throw new ContentPackException(
                                        current + " was not an integer but should! "
                                                + " Did you use your predicate saver correctly?");
                            }
                        }
                        if (current.equals("(") && names.equals("list")) {
                            readInt = true;
                            continue;
                        }
                        final boolean isBracket = current.equals(")") || current.equals("(");
                        if (Character.isWhitespace(letter) || current.equals("!") || isBracket) {
                            names = "";
                            continue;
                        }   
                        names += current;
                    }
                }

                if (valueToParse != null && !valueToParse.isEmpty()
                        && !valueToParse.equalsIgnoreCase("true")) {
                    predicate = LogicParser.predicate(valueToParse, endInfo);
                }

                final Map<SEProperty, Object> values = new HashMap<>();

                for (final String value : entry.getValue()) {

                    final String[] valuetoChange = value.split("\\.");
                    final SEProperty property = (SEProperty) startInfo
                            .getProperty(valuetoChange[0]);

                    Object valueToSet = valuetoChange[1];
                    if (valuetoChange[1].equalsIgnoreCase("false")
                            || valuetoChange[1].equalsIgnoreCase("true")) {
                        valueToSet = Boolean.valueOf(valuetoChange[1]);
                    }
                    values.put(property, valueToSet);
                }

                properties.add(new ConfigProperty(predicate, values));

            }
            CHANGECONFIGS.put(pair, properties);
        }
    }
}
