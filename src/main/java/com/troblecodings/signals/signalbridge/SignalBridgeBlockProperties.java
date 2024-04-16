package com.troblecodings.signals.signalbridge;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.enums.SignalBridgeType;

public class SignalBridgeBlockProperties {

    public transient SignalBridgeType bridgeType;
    private String type;

    public SignalBridgeType getType() {
        if (bridgeType == null) {
            if (type == null) {
                OpenSignalsMain.exitMinecraftWithMessage(
                        "You need to define a Type for the SignalBridge! Valid Types: "
                                + SignalBridgeType.values());
            }
            bridgeType = Enum.valueOf(SignalBridgeType.class, type);
        }
        return bridgeType;
    }
}