package com.troblecodings.signals.signalbox.config;

import com.troblecodings.signals.handler.SignalStateInfo;

public class ResetInfo {

    public final SignalStateInfo current;
    public final boolean isRepeater;

    public ResetInfo(SignalStateInfo current, boolean isRepeater) {
        this.current = current;
        this.isRepeater = isRepeater;
    }
}