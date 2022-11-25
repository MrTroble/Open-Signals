package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public class ConfigInfo {

    public final SignalTileEnity current;
    public final SignalTileEnity next;
    public final int speed;
    public PathType type = PathType.NONE;

    public ConfigInfo(final SignalTileEnity current, final SignalTileEnity next, final int speed) {
        this.current = current;
        this.next = next;
        this.speed = speed;
    }

}