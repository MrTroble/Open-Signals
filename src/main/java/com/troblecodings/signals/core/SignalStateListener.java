package com.troblecodings.signals.core;

import com.troblecodings.signals.handler.SignalStateInfo;

public interface SignalStateListener {

    public void update(final SignalStateInfo info, final boolean removed);

}
