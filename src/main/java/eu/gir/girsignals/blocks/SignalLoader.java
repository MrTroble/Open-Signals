package eu.gir.girsignals.blocks;

import java.util.Map;

import com.troblecodings.signals.contentpacks.SignalSystemParser;

import eu.gir.girsignals.init.GIRBlocks;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadInternSignals() {
        loadSignalsfromDirectory("/assets/girsignals/signalsystems");
    }

    public static void loadSignalsfromDirectory(final String directory) {
        final Map<String, SignalSystemParser> signals = SignalSystemParser
                .getSignalSystems(directory);
        signals.forEach((filename, properties) -> GIRBlocks.blocksToRegister
                .add(properties.createSignalSystem(filename)));
    }
}