package com.troblecodings.signals.core;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.signalbox.Point;

public class NetworkBufferWrappers {

    public static final BiConsumer<WriteBuffer, Point> POINT_CONSUMER = (buffer, point) -> point
            .writeNetwork(buffer);

    public static final Function<ReadBuffer, Point> POINT_FUNCTION = (buffer) -> Point.of(buffer);

}
