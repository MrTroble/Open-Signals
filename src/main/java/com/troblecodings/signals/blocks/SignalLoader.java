package com.troblecodings.signals.blocks;

import java.util.Map;

import com.troblecodings.signals.contentpacks.SignalSystemParser;
import com.troblecodings.signals.init.SignalBlocks;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadInternSignals() {
        final Map<String, SignalSystemParser> signals = SignalSystemParser
                .getSignalSystems("/assets/girsignals/signalsystems");
        signals.forEach((filename, properties) -> {
            SignalBlocks.blocksToRegister.add(properties.createNewSignalSystem(filename));
        });
    }
}