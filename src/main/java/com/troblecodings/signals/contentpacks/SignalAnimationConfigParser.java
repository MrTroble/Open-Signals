package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.animation.SignalAnimationState;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.parser.FunctionParsingInfo;

public class SignalAnimationConfigParser {

    private Map<String, List<SignalAnimationConfig>> animations;

    private static final Gson GSON = new Gson();

    public static final Map<Signal, Map<String, List<SignalAnimationState>>> ALL_ANIMATIONS = new HashMap<>();

    public static void loadAllAnimations() {
        OpenSignalsMain.contentPacks.getFiles("animations").forEach(entry -> {
            final String signalName = entry.getKey().replace(".json", "").toLowerCase();
            final Signal signal = Signal.SIGNALS.get(signalName);
            if (signal == null)
                OpenSignalsMain
                        .exitMinecraftWithMessage("There doesn't exists a signal with the name '"
                                + signalName + "'! Valid Signals are: " + Signal.SIGNALS.keySet());
            if (ALL_ANIMATIONS.containsKey(signal))
                OpenSignalsMain.exitMinecraftWithMessage(
                        "There are already existing animations for " + signal + "!");
            final FunctionParsingInfo info = new FunctionParsingInfo(signal);

            final SignalAnimationConfigParser parser = GSON.fromJson(entry.getValue(),
                    SignalAnimationConfigParser.class);
            final Map<String, List<SignalAnimationState>> modelToAnimation = new HashMap<>();
            parser.animations.forEach((modelName, configs) -> {
                final List<SignalAnimationState> animatinos = new ArrayList<>();
                for (final SignalAnimationConfig config : configs) {
                    animatinos.add(config.createAnimation(info).with(modelName));
                }
                if (!animatinos.isEmpty())
                    modelToAnimation.put(modelName, animatinos);
            });
            if (!modelToAnimation.isEmpty())
                ALL_ANIMATIONS.put(signal, modelToAnimation);
        });
    }

}