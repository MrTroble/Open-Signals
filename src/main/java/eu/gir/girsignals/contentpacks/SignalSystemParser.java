package eu.gir.girsignals.contentpacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal.SignalProperties;
import eu.gir.girsignals.blocks.Signal.SignalPropertiesBuilder;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.items.Placementtool;

public class SignalSystemParser {

    private List<SignalProperties> systemProperties;
    @SuppressWarnings("rawtypes")
    private List<SEPropertyParser> seProperties;

    public List<SignalProperties> getSystemProperties() {
        return systemProperties;
    }

    @SuppressWarnings("rawtypes")
    public List<SEPropertyParser> seProperties() {
        return seProperties;
    }

    public static List<SignalProperties> getSignalSystems(final String directory) {

        final Gson gson = new Gson();

        final Map<String, String> systems = GIRFileReader.readallFilesfromDierectory(directory);

        final List<SignalProperties> properties = new ArrayList<>();

        if (systems == null) {
            GirsignalsMain.log.warn("Can't read out signalsystems from " + directory + "!");
            return properties;
        }

        systems.forEach((name, property) -> {

            Placementtool tool = GIRItems.PLACEMENT_TOOL;

            final SignalProperties signalProperty = gson.fromJson(property, SignalProperties.class);

            if (signalProperty.placementToolName.equalsIgnoreCase("SIGN_PLACEMENTTOOL"))
                tool = GIRItems.SIGN_PLACEMENT_TOOL;

            if (signalProperty.canLink) {
                properties.add(new SignalPropertiesBuilder(tool, name.replace(".json", ""))
                        .height(signalProperty.height).offsetX(signalProperty.offsetX)
                        .offsetY(signalProperty.offsetY)
                        .signHeight(signalProperty.customNameRenderHeight)
                        .signScale(signalProperty.signScale).signWidth(signalProperty.signWidth)
                        .build());
            } else {
                properties.add(new SignalPropertiesBuilder(tool, name.replace(".json", ""))
                        .height(signalProperty.height).offsetX(signalProperty.offsetX)
                        .offsetY(signalProperty.offsetY)
                        .signHeight(signalProperty.customNameRenderHeight)
                        .signScale(signalProperty.signScale).signWidth(signalProperty.signWidth)
                        .noLink().build());
            }

        });

        return properties;
    }

}
