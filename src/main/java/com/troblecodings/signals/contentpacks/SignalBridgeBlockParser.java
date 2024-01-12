package com.troblecodings.signals.contentpacks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.troblecodings.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.SignalBridgeType;

public class SignalBridgeBlockParser {

    private String type;

    private static final Gson GSON = new Gson();
    public static final Map<SignalBridgeType, List<SignalBridgeBasicBlock>> SIGNAL_BRIDGE_BLOCKS = //
            new HashMap<>();

    public static void loadSignalBridgeBlocks() {
        final Type typeOfMap = new TypeToken<Map<String, SignalBridgeBlockParser>>() {
        }.getType();
        OpenSignalsMain.contentPacks.getFiles("signalbridge").forEach(entry -> {
            final Map<String, SignalBridgeBlockParser> stats = GSON.fromJson(entry.getValue(),
                    typeOfMap);
            stats.forEach((blockName, properties) -> {
                final SignalBridgeType bridgeType = Enum.valueOf(SignalBridgeType.class,
                        properties.type);
                final List<SignalBridgeBasicBlock> blocks = SIGNAL_BRIDGE_BLOCKS
                        .computeIfAbsent(bridgeType, _u -> new ArrayList<>());
                blocks.add(bridgeType.createNewBlock(blockName));
            });
        });
    }

}
