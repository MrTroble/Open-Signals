package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;

public interface INetworkSavable extends ISaveable {
	
    public void readNetwork(final ByteBuffer buffer);
    
    public void writeNetwork(final ByteBuffer buffer);

}
