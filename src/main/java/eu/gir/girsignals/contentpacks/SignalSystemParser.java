package eu.gir.girsignals.contentpacks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalProperties;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.items.Placementtool;

public class SignalSystemParser {

    private List<SignalProperties> systemProperties;
    private List<SEPropertyParser> seProperties;

    private static transient List<Signal> SYSTEMS;

    public List<SEPropertyParser> getSEProperties() {
        return seProperties;
    }

    public static List<Signal> getSignalSystems() {
        return ImmutableList.copyOf(SYSTEMS);
    }

    public static Map<String, SignalSystemParser> getSignalSystems(final String directory) {

        final Gson gson = new Gson();

        final Map<String, String> systems = GIRFileReader.readallFilesfromDierectory(directory);

        final Map<String, SignalSystemParser> properties = new HashMap<>();

        if (systems == null) {
            GirsignalsMain.log.warn("Can't read out signalsystems from " + directory + "!");
            return properties;
        }

        systems.forEach((name, property) -> properties.put(name,
                gson.fromJson(property, SignalSystemParser.class)));

        return properties;
    }

    public Signal createNewSignalSystem(final String fileName) {

        SignalProperties signalProperty = systemProperties.get(0);

        Placementtool tool = GIRItems.PLACEMENT_TOOL;

        if (signalProperty.placementToolName.equalsIgnoreCase("SIGN_PLACEMENT_TOOL"))
            tool = GIRItems.SIGN_PLACEMENT_TOOL;

        if (signalProperty.canLink) {
            signalProperty = Signal
                    .builder(tool, fileName.replace(".json", "").replace("_", "").toLowerCase())
                    .height(signalProperty.height).offsetX(signalProperty.offsetX)
                    .offsetY(signalProperty.offsetY)
                    .signHeight(signalProperty.customNameRenderHeight)
                    .signScale(signalProperty.signScale).signWidth(signalProperty.signWidth)
                    .build();
        } else {
            signalProperty = Signal
                    .builder(tool, fileName.replace(".json", "").replace("_", "").toLowerCase())
                    .height(signalProperty.height).offsetX(signalProperty.offsetX)
                    .offsetY(signalProperty.offsetY)
                    .signHeight(signalProperty.customNameRenderHeight)
                    .signScale(signalProperty.signScale).signWidth(signalProperty.signWidth)
                    .noLink().build();
        }

        return new Signal(signalProperty);
    }
}