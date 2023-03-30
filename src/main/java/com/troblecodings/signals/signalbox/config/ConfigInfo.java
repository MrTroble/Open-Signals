package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.handler.SignalStateInfo;

public class ConfigInfo {

    public final SignalStateInfo currentinfo;
    public final SignalStateInfo nextinfo;
    public final int speed;
    public final String zs2Value;
    public PathType type = PathType.NONE;

    public ConfigInfo(final SignalStateInfo currentinfo, final SignalStateInfo nextinfo,
            final int speed, final String zs2Value) {
        this.currentinfo = currentinfo;
        this.nextinfo = nextinfo;
        this.speed = speed;
        this.zs2Value = zs2Value;
    }

}