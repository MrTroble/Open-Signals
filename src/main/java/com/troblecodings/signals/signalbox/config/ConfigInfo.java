package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

public class ConfigInfo {

    public final SignalTileEntity current;
    public final SignalTileEntity next;
    public final int speed;
    public PathType type = PathType.NONE;

    public ConfigInfo(final SignalTileEntity current, final SignalTileEntity next, final int speed) {
        this.current = current;
        this.next = next;
        this.speed = speed;
    }

}