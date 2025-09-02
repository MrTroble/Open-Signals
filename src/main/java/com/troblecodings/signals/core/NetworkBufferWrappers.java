package com.troblecodings.signals.core;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public final class NetworkBufferWrappers {

    public static final BiConsumer<WriteBuffer, Point> POINT_CONSUMER = (buffer, point) -> point
            .writeNetwork(buffer);

    public static final BiConsumer<WriteBuffer, SignalBoxNode> SIGNALBOXNODE_CONSUMER = (buffer,
            node) -> node.writeNetwork(buffer);

    public static final BiConsumer<WriteBuffer, SignalBoxNode> POINT_SIGNALBOXNODE_CONSUMER = (
            buffer, node) -> {
        node.getPoint().writeNetwork(buffer);
        node.writeNetwork(buffer);
    };

    public static final BiConsumer<WriteBuffer, PosIdentifier> POS_IDENTIFIER_CONSUMER = (buffer,
            posIdent) -> posIdent.writeNetwork(buffer);

    public static BiConsumer<WriteBuffer, SEProperty> getSEPropertyConsumer(final Signal signal) {
        return (buffer, prop) -> buffer.putByte((byte) signal.getIDFromProperty(prop));
    }

    public static final BiConsumer<WriteBuffer, PathEntryType<?>> PATHENTRYTYPE_CONSUMER = (buf,
            type) -> buf.putByte((byte) type.getID());

    public static final BiConsumer<WriteBuffer, IPathEntry<?>> PATHENTRY_CONSUMER = (buf,
            entry) -> entry.writeNetwork(buf);

    public static final Function<ReadBuffer, Point> POINT_FUNCTION = buffer -> Point.of(buffer);

    public static final Function<ReadBuffer, PosIdentifier> POS_IDENTIFIER_FUNCTION = buffer -> PosIdentifier
            .of(buffer);

    public static final Function<ReadBuffer, PathEntryType<?>> PATHENTRYTYPE_FUNCTION = buf -> PathEntryType.ALL_ENTRIES
            .get(buf.getByteToUnsignedInt());

    public static Function<ReadBuffer, SignalBoxNode> getSignalBoxNodeFunc() {
        return buffer -> getSignalBoxNodeFunc(Point.of(buffer)).apply(buffer);
    }

    public static Function<ReadBuffer, SignalBoxNode> getSignalBoxNodeFunc(final Point point) {
        return buffer -> {
            final SignalBoxNode node = new SignalBoxNode(point);
            node.readNetwork(buffer);
            return node;
        };
    }

    public static Function<ReadBuffer, SEProperty> getSEPropertyFunc(final Signal signal) {
        return buffer -> signal.getPropertyByIndex(buffer.getByteToUnsignedInt());
    }

}
