package eu.gir.girsignals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.contentpacks.SignalSystemParser;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;

public class SignalLoader {

    @SuppressWarnings("rawtypes")
    private static final Map<Signal, List<SEProperty>> SIGNALS = new HashMap<>();

    @SuppressWarnings("rawtypes")
    public static Map<Signal, List<SEProperty>> getSignals() {
        return ImmutableMap.copyOf(SIGNALS);
    }

    public static void loadInternSignals() {
        loadSignals(SignalSystemParser.getSignalSystems("/assets/girsignals/signalsystems"));

    }

    @SuppressWarnings("rawtypes")
    public static void loadSignals(final Map<String, SignalSystemParser> signals) {

        signals.forEach((filename, properties) -> {

            final Signal signalType = properties.createNewSignalSystem(filename);

            final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(signalType);

            final List<SEProperty> property = new ArrayList<>();

            if (properties.getSEProperties() != null) {
                properties.getSEProperties().forEach(
                        seproperty -> property.add(seproperty.createSEProperty(parsingInfo)));
            }

            SIGNALS.put(signalType, property);
        });
    }
}