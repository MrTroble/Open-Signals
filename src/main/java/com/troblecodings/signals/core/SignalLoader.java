package com.troblecodings.signals.core;

import java.util.Map;

import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.contentpacks.SignalSystemParser;
import com.troblecodings.signals.init.OSBlocks;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadAllSignals() {
        final Map<String, SignalSystemParser> signals = SignalSystemParser
                .getSignalSystems("/assets/girsignals/signalsystems");
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