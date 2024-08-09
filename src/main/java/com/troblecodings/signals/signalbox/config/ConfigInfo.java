package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.signalbox.PathwayData;

public class ConfigInfo {

    public final SignalStateInfo currentinfo;
    public final SignalStateInfo nextinfo;
    public final int speed;
    public final String zs2Value;
    public final PathType type;
    public final boolean isSignalRepeater;

    public ConfigInfo(final SignalStateInfo currentinfo, final SignalStateInfo nextinfo,
            final PathwayData data) {
        this(currentinfo, nextinfo, data, false);
    }

    public ConfigInfo(final SignalStateInfo currentinfo, final SignalStateInfo nextinfo,
            final PathwayData data, final boolean isSignalRepeater) {
        this.currentinfo = currentinfo;
        this.nextinfo = nextinfo;
        this.speed = data.getSpeed();
        this.zs2Value = data.getZS2Value();
        this.type = data.getPathType();
        this.isSignalRepeater = isSignalRepeater;
    }

}