package com.troblecodings.signals.contentpacks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.Signal.SignalPropertiesBuilder;
import com.troblecodings.signals.utils.FileReader;

public class SignalSystemParser {

    private SignalPropertiesBuilder systemProperties;
    private List<SEPropertyParser> seProperties;

    public List<SEPropertyParser> getSEProperties() {
        return seProperties;
    }

    public static Map<String, SignalSystemParser> getSignalSystems(final String directory) {

        final Gson gson = new Gson();

        final Map<String, String> systems = FileReader.readallFilesfromDierectory(directory);

        final Map<String, SignalSystemParser> properties = new HashMap<>();

        if (systems == null) {
            SignalsMain.log.warn("Can't read out signalsystems from " + directory + "!");
            return properties;
        }

        systems.forEach((name, property) -> properties.put(name,
                gson.fromJson(property, SignalSystemParser.class)));

        return properties;
    }

    public Signal createNewSignalSystem(final String fileName) {
        Signal.nextConsumer = list -> seProperties
                .forEach(prop -> list.add(prop.createSEProperty()));
        return new Signal(systemProperties
                .typename(fileName.replace(".json", "").replace("_", "").toLowerCase()).build());
    }
}