package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;

public interface INetworkSavable extends ISaveable {

    public void readNetwork(final ReadBuffer buffer);

    public void writeNetwork(final WriteBuffer buffer);

}
