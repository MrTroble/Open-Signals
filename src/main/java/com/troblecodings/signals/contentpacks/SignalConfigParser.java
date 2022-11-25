package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.utils.FileReader;

public class SignalConfigParser {

    private String currentSignal;
    private String nextSignal;
    private Map<String, String> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public String getNextSignalSystem() {
        return nextSignal;
    }

    public Map<String, String> getValuesToChange() {
        return values;
    }

    public static final Map<SignalPair, List<ConfigProperty>> SIGNALCONFIGS = new HashMap<>();

    private static final Gson GSON = new Gson();

    public static void loadConfigs() {
        loadNormalConfigs("/assets/girsignals/signalconfigs/normal");
    }

    @SuppressWarnings("rawtypes")
    public static void loadNormalConfigs(final String directory) {
        final Map<String, String> files = FileReader.readallFilesfromDierectory(directory);
        files.forEach((_u, content) -> {
            final SignalConfigParser parser = GSON.fromJson(content, SignalConfigParser.class);
            Signal start = null;
            Signal end = null;
            for (final Signal signal : Signal.SIGNALLIST) {
                final String name = signal.getSignalTypeName();

                if (start == null && parser.getCurrentSignal().equalsIgnoreCase(name))
                    start = signal;

                if (end == null && parser.getNextSignalSystem().equalsIgnoreCase(name))
                    end = signal;

                if (start != null && end != null)
                    break;
            }
            if (start == null || end == null) {
                throw new ContentPackException(
                        String.format("The signal [%d] or the signal [%s] don't exists!",
                                parser.getCurrentSignal(), parser.getNextSignalSystem()));
            }
            final SignalPair pair = new SignalPair(start, end);
            final FunctionParsingInfo startInfo = new FunctionParsingInfo(start);
            final FunctionParsingInfo endInfo = new FunctionParsingInfo(end);
            final List<ConfigProperty> properties = new ArrayList<>();

            for (Map.Entry<String, String> entry : parser.getValuesToChange().entrySet()) {

                final Predicate predicate = LogicParser.predicate(entry.getKey(), endInfo);
                final String[] value = entry.getValue().split("\\.");
                final SEProperty property = (SEProperty) startInfo.getProperty(value[0]);

                properties.add(new ConfigProperty(predicate, property, value[1]));

            }
            SIGNALCONFIGS.put(pair, properties);
        });
    }
}