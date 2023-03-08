package com.troblecodings.signals.contentpacks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.SubsidiaryType;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.properties.ConfigProperty;

public class SubsidiarySignalParser {

    private String currentSignal;
    private List<String> zs1Values;
    private List<String> zs7Values;
    private List<String> zs8Values;

    public String getCurrentSignal() {
        return currentSignal;
    }

    public List<String> getZs1Values() {
        return zs1Values;
    }

    public List<String> getZs7Values() {
        return zs7Values;
    }

    public List<String> getZs8Values() {
        return zs8Values;
    }

    public static transient final Map<Signal, Map<SubsidiaryType, ConfigProperty>> SUBSIDIARY_SIGNALS = new HashMap<>();

    private static transient final Gson GSON = new Gson();

    public static void loadAllSubsidiarySignals() {
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
            convertToProperites(signal, info, parser.getZs1Values(), SubsidiaryType.ZS1);
            convertToProperites(signal, info, parser.getZs7Values(), SubsidiaryType.ZS7);
            convertToProperites(signal, info, parser.getZs8Values(), SubsidiaryType.ZS8);
        });
    }

    private static void convertToProperites(final Signal signal, final FunctionParsingInfo info,
            final List<String> values, final SubsidiaryType type) {
        if (values == null)
            return;
        final Map<SEProperty, String> allValues = new HashMap<>();
        values.forEach(str -> {
            final String[] value = str.split("\\.");
            allValues.put((SEProperty) info.getProperty(value[0]), value[1]);
        });
        final Map<SubsidiaryType, ConfigProperty> properties = SUBSIDIARY_SIGNALS
                .computeIfAbsent(signal, _u -> new HashMap<>());
        properties.put(type, new ConfigProperty(allValues));
        SUBSIDIARY_SIGNALS.put(signal, properties);
    }
}