package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalStateInfo;

public class ConfigInfo {

    public final SignalStateInfo currentinfo;
    public final SignalStateInfo nextinfo;
    public final int speed;
    public final String zs2Value;
    public final PathType type;
    public final boolean isSignalRepeater;

    public ConfigInfo(final SignalStateInfo currentinfo, final SignalStateInfo nextinfo,
            final int speed, final String zs2Value, final PathType type) {
        this(currentinfo, nextinfo, speed, zs2Value, type, false);
    }

    public ConfigInfo(final SignalStateInfo currentinfo, final SignalStateInfo nextinfo,
            final int speed, final String zs2Value, final PathType type,
            final boolean isSignalRepeater) {
        this.currentinfo = currentinfo;
        this.nextinfo = nextinfo;
        this.speed = speed;
        this.zs2Value = zs2Value;
        this.type = type;
        this.isSignalRepeater = isSignalRepeater;
    }
}