package com.troblecodings.signals.contentpacks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.SubsidiaryState;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.properties.ConfigProperty;

public class SubsidiarySignalParser {

    private String currentSignal;
    private Map<String, List<String>> allStates;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public Map<String, List<String>> getAllStates() {
        return allStates;
    }

    public static final Map<Signal, Map<SubsidiaryState, ConfigProperty>> SUBSIDIARY_SIGNALS = new HashMap<>();

    private static final Gson GSON = new Gson();

    private static void loadSubsidiaryStates() {
        OpenSignalsMain.contentPacks.getFiles("signalconfigs/subsidiaryenums").forEach(entry -> {
            final SubsidiaryEnumParser parser = GSON.fromJson(entry.getValue(),
                    SubsidiaryEnumParser.class);
            parser.getSubsidiaryStates().forEach(state -> new SubsidiaryState(state));
        });
    }

    public static void loadAllSubsidiarySignals() {
        loadSubsidiaryStates();
        OpenSignalsMain.contentPacks.getFiles("signalconfigs/subsidiary").forEach(entry -> {
            final SubsidiarySignalParser parser = GSON.fromJson(entry.getValue(),
                    SubsidiarySignalParser.class);
            final Signal signal = Signal.SIGNALS.get(parser.getCurrentSignal().toLowerCase());
            if (signal == null)
                throw new ContentPackException("There doesn't exists a signal with the name '"
                        + parser.getCurrentSignal() + "'!");
            if (SUBSIDIARY_SIGNALS.containsKey(signal))
                throw new ContentPackException(
                        "There already exists a Subsidiary Config for " + signal + "!");
            final FunctionParsingInfo info = new FunctionParsingInfo(signal);
            parser.getAllStates().forEach((name, properties) -> {
                convertToProperites(signal, info, properties, name);
            });
        });
    }

    private static void convertToProperites(final Signal signal, final FunctionParsingInfo info,
            final List<String> values, final String enumName) {
        if (values == null)
            return;
        SubsidiaryState enumState = null;
        for (int i = 0; i < SubsidiaryState.ALL_STATES.size(); i++) {
            final SubsidiaryState current = SubsidiaryState.ALL_STATES.get(i);
            if (current.getName().equalsIgnoreCase(enumName)) {
                enumState = current;
                break;
            }
        }
        if (enumState == null)
            throw new ContentPackException(enumName + " is not a valid Subsidiary State!");
        final Map<SEProperty, String> allValues = new HashMap<>();
        values.forEach(str -> {
            final String[] value = str.split("\\.");
            allValues.put((SEProperty) info.getProperty(value[0]), value[1]);
        });
        final Map<SubsidiaryState, ConfigProperty> properties = SUBSIDIARY_SIGNALS
                .computeIfAbsent(signal, _u -> new HashMap<>());
        properties.put(enumState, new ConfigProperty(allValues));
        SUBSIDIARY_SIGNALS.put(signal, properties);
    }

    private static class SubsidiaryEnumParser {

        private List<String> subsidiaryStates;

        public List<String> getSubsidiaryStates() {
            return subsidiaryStates;
        }
    }
}