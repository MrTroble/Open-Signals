package com.troblecodings.signals.core;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.network.SignalBoxNetworkHandler;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public final class NetworkBufferWrappers {

    private NetworkBufferWrappers() {

    }

    public static final BiConsumer<WriteBuffer, SignalBoxNode> POINT_SIGNALBOXNODE_CONSUMER = (
            buffer, node) -> {
        node.getPoint().writeNetwork(buffer);
        node.writeNetwork(buffer);
    };

    public static BiConsumer<WriteBuffer, SEProperty> getSEPropertyConsumer(final Signal signal) {
        return (buffer, prop) -> buffer.putByte((byte) signal.getIDFromProperty(prop));
    }

    public static final BiConsumer<WriteBuffer, PathEntryType<?>> PATHENTRYTYPE_CONSUMER = (buf,
            type) -> buf.putByte((byte) type.getID());

    public static final Function<ReadBuffer, PathEntryType<?>> PATHENTRYTYPE_FUNCTION = buf -> PathEntryType.ALL_ENTRIES
            .get(buf.getByteToUnsignedInt());

    public static Function<ReadBuffer, SignalBoxNode> getSignalBoxNodeFunc(
            final SignalBoxNetworkHandler network) {
        return buffer -> getSignalBoxNodeFunc(
                ReadBuffer.getINetworkSaveableFunction(Point.class).apply(buffer), network)
                        .apply(buffer);
    }

    public static Function<ReadBuffer, SignalBoxNode> getSignalBoxNodeFunc(final Point point,
            final SignalBoxNetworkHandler network) {
        return buffer -> {
            final SignalBoxNode node = new SignalBoxNode(point, network);
            node.readNetwork(buffer);
            return node;
        };
    }

    public static Function<ReadBuffer, SEProperty> getSEPropertyFunc(final Signal signal) {
        return buffer -> signal.getPropertyByIndex(buffer.getByteToUnsignedInt());
    }

}
