package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.contentpacks.SignalSystemParser;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;

public class SignalLoader {

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
        });
    }
}