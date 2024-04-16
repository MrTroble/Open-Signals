package com.troblecodings.signals.core;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.SignalSystemParser;
import com.troblecodings.signals.init.OSBlocks;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadAllSignals() {
        SignalSystemParser.getAllSignals().forEach((filename, properties) -> {
            try {
                OSBlocks.loadBlock(properties.createSignalSystem(filename),
                        filename.replace(".json", "").replace("_", "").toLowerCase().trim());
            } catch (final Exception e) {
                OpenSignalsMain.exitMinecraftWithMessage(
                        String.format("Error in file %s caused by parsing! " + e, filename));
            }
        });
    }
}