package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

public interface Observer {

    public void update(final ByteBuffer buffer);

}