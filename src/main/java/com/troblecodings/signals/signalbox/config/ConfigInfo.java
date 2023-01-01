package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.statehandler.SignalStateInfo;

public class ConfigInfo {

    public final SignalStateInfo currentinfo;
    public final SignalStateInfo nextinfo;
    public final int speed;
    public PathType type = PathType.NONE;

    public ConfigInfo(final SignalStateInfo nextinfo, final SignalStateInfo currentinfo, final int speed) {
        this.currentinfo = currentinfo;
        this.nextinfo = nextinfo;
        this.speed = speed;
    }

}