package com.troblecodings.signals.blocks;

import java.util.Map;

import com.troblecodings.signals.contentpacks.SignalSystemParser;

public final class SignalLoader {

    private SignalLoader() {
    }

    public static void loadInternSignals() {
        final Map<String, SignalSystemParser> signals = SignalSystemParser
                .getSignalSystems("/assets/girsignals/signalsystems");
        signals.forEach((filename, properties) -> {
            Signal.SIGNALLIST.add(properties.createNewSignalSystem(filename));
        });
    }
}