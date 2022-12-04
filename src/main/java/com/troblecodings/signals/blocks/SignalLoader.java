package com.troblecodings.signals.blocks;

import java.util.Map;

import com.troblecodings.signals.contentpacks.SignalSystemParser;
import com.troblecodings.signals.init.OSBlocks;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadInternSignals() {
        loadSignalsfromDirectory("/assets/girsignals/signalsystems");
    }

    public static void loadSignalsfromDirectory(final String directory) {
        final Map<String, SignalSystemParser> signals = SignalSystemParser
                .getSignalSystems(directory);
        signals.forEach((filename, properties) -> OSBlocks.loadBlock(
                properties.createSignalSystem(filename), filename.replace(".json", " ")));
    }
}