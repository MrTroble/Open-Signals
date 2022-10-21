package eu.gir.girsignals.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.contentpacks.SignalSystemParser;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;

public class SignalLoader {

    private static final List<Signal> SIGNALS = new ArrayList<>();

    public static List<Signal> getSignals() {
        return ImmutableList.copyOf(SIGNALS);
    }

    public static void loadInternSignals() {
        loadSignals(SignalSystemParser.getSignalSystems("/assets/girsignals/signalsystems"));
    }

    @SuppressWarnings("rawtypes")
    public static void loadSignals(final Map<String, SignalSystemParser> signals) {

        signals.forEach((filename, properties) -> {

            final Signal signal = properties.createNewSignalSystem(filename);

            final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(signal);

            final List<SEProperty> property = new ArrayList<>();

            if (properties.getSEProperties() != null && !properties.getSEProperties().isEmpty()) {
                properties.getSEProperties().forEach(
                        seproperty -> property.add(seproperty.createSEProperty(parsingInfo)));

                signal.appendSEProperty(property);
            }
            SIGNALS.add(signal);
        });
    }
}