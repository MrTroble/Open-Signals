package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.blocks.ConfigProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalPair;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.utils.FileReader;

public class TwoSignalConfigParser {

    private String currentSignal;
    private String nextSignal;
    private Map<String, String> values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public String getNextSignal() {
        return nextSignal;
    }

    public Map<String, String> getValuesToChange() {
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

            Signal start = Signal.SIGNALS.get(parser.getCurrentSignal().toLowerCase());
            Signal end = Signal.SIGNALS.get(parser.getNextSignal().toLowerCase());
            if (start == null || end == null) {
                SignalsMain.getLogger().warn("The signal '" + parser.getCurrentSignal()
                        + "' or the signal '" + parser.getNextSignal() + "' doen't exists! "
                        + "This config with filename '" + files.getKey() + "' will be skiped!");
                continue;
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
            CHANGECONFIGS.put(pair, properties);
        }
    }
}