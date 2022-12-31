package com.troblecodings.signals.core;

import java.util.Map;

import com.troblecodings.signals.contentpacks.ContentPackException;
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
        signals.forEach((filename, properties) -> {
            try {
                OSBlocks.loadBlock(properties.createSignalSystem(filename),
                        filename.replace(".json", "").replace("_", "").toLowerCase().trim());
            } catch (final Exception e) {
                throw new ContentPackException(
                        String.format("Error in file %s caused by parsing!", filename), e);
            }
        });
    }
}