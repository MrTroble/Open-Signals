package eu.gir.girsignals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.contentpacks.SignalSystemParser;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;

public class SignalLoader extends Signal {

    @SuppressWarnings("rawtypes")
    private static final List<SEProperty> SE_PROPERTIES = new ArrayList<>();

    private static final Map<String, Signal> TRANSLATION_TABLE = new HashMap<>();

    private static final List<Signal> SIGNALS = new ArrayList<>(Signal.SIGNALLIST);

    static {
        SIGNALS.forEach(signal -> TRANSLATION_TABLE.put(signal.getSignalTypeName(), signal));
    }

    private SignalLoader(SignalProperties prop) {
        super(prop);
    }

    @SuppressWarnings("rawtypes")
    public static List<SEProperty> getSEProperties() {
        return ImmutableList.copyOf(SE_PROPERTIES);
    }

    public static void loadInternSignals() {
        loadSignals(SignalSystemParser.getSignalSystems("/assets/girsignals/signalsystems"));
    }

    public static void loadSignals(final Map<String, SignalSystemParser> signals) {

        signals.forEach((filename, properties) -> {

            properties.createNewSignalSystem(filename);

            Signal signaltype = null;

            for (final Map.Entry<String, Signal> entry : TRANSLATION_TABLE.entrySet()) {

                final String signalname = entry.getKey();
                final Signal signal = entry.getValue();

                if (filename.replace(".json", "").equalsIgnoreCase(signalname)) {

                    signaltype = signal;
                }
            }
            if (signaltype == null) {

                GirsignalsMain.log.error("There doesn't exists a signalsystem named "
                        + filename.replace(".json", "") + "!");
                return;
            }

            final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(signaltype);

            if (properties != null)
                properties.getSEProperties().forEach(
                        seProperty -> SE_PROPERTIES.add(seProperty.createSEProperty(parsingInfo)));
        });
    }
}