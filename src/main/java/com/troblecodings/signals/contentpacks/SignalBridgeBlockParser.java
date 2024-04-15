package com.troblecodings.signals.contentpacks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.SignalBridgeType;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBlockProperties;

public final class SignalBridgeBlockParser {

    private SignalBridgeBlockParser() {
    }

    private static final Gson GSON = new Gson();
    public static final Map<SignalBridgeType, List<SignalBridgeBasicBlock>> SIGNAL_BRIDGE_BLOCKS =
            new HashMap<>();

    @SuppressWarnings("serial")
    public static void loadSignalBridgeBlocks() {
        final Type typeOfMap = new TypeToken<Map<String, SignalBridgeBlockProperties>>() {
        }.getType();
        OpenSignalsMain.contentPacks.getFiles("signalbridge").forEach(entry -> {
            final Map<String, SignalBridgeBlockProperties> stats =
                    GSON.fromJson(entry.getValue(), typeOfMap);
            stats.forEach((blockName, properties) -> {
                final SignalBridgeType bridgeType = properties.getType();
                final List<SignalBridgeBasicBlock> blocks =
                        SIGNAL_BRIDGE_BLOCKS.computeIfAbsent(bridgeType, _u -> new ArrayList<>());
                blocks.add(bridgeType.createNewBlock(blockName, properties));
            });
        });
    }

}