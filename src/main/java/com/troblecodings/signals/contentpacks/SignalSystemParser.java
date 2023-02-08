package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.SignalPropertiesBuilder;
import com.troblecodings.signals.parser.FunctionParsingInfo;

public class SignalSystemParser {

    private SignalPropertiesBuilder systemProperties;
    private List<SEPropertyParser> seProperties;

    private static transient final Gson GSON = new Gson();

    public List<SEPropertyParser> getSEProperties() {
        return seProperties;
    }

    public static Map<String, SignalSystemParser> getSignalSystems(final String directory) {

        final Map<String, String> systems = FileReader.readallFilesfromDierectory(directory);

        final Map<String, SignalSystemParser> properties = new HashMap<>();

        if (systems.isEmpty()) {
            OpenSignalsMain.getLogger().warn("No signalsystems found at '" + directory + "'!");
            return properties;
        }

        systems.forEach((name, property) -> {
            properties.put(name, GSON.fromJson(property, SignalSystemParser.class));
        });

        return properties;
    }

    public Signal createSignalSystem(final String fileName) {

        final String name = fileName.replace(".json", "").replace("_", "").toLowerCase().trim();

        final List<SEProperty> properties = new ArrayList<>();

        final FunctionParsingInfo info = new FunctionParsingInfo(name, properties);
        try {
            seProperties.forEach(prop -> {
                properties.add(prop.createSEProperty(info));
                if (properties.size() > 254) {
                    OpenSignalsMain.getLogger()
                            .info("Congratulations, you are probably one of the first people on "
                                    + "earth to try to register more than 254 SEProperties. We "
                                    + "don't want to ruin your work, but 254 is the maximum "
                                    + "number of SEProperties!");
                    throw new ContentPackException(
                            "You added to many properties to your signalsystem. Max. is 254!");
                }
            });
        } catch (final Exception e) {
            throw new ContentPackException(
                    String.format("Error in file %s caused by parsing!", fileName), e);
        }
        Signal.nextConsumer = list -> {
            list.addAll(properties);
        };

        return new Signal(systemProperties.build(info));
    }
}