package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.signals.core.BufferFactory;

public interface INetworkSavable extends ISaveable {

    public void readNetwork(final BufferFactory buffer);

    public void writeNetwork(final BufferFactory buffer);

}
