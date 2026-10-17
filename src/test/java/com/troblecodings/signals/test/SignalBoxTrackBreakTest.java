package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Path;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.util.Rotation;

public class SignalBoxTrackBreakTest {

    private static final ModeSet BREAK_NONE = new ModeSet(EnumGuiMode.TRACK_BREAK, Rotation.NONE);
    private static final ModeSet BREAK_180 =
            new ModeSet(EnumGuiMode.TRACK_BREAK, Rotation.CLOCKWISE_180);

    @BeforeAll
    public static void setUpFactory() {
        SignalBoxFactory.setUpFactoryForTests();
    }

    private SignalBoxNode addBreak(final SignalBoxGrid grid, final Point point,
            final ModeSet mode) {
        final SignalBoxNode node = grid.getOrCreateNode(point);
        node.add(mode);
        return node;
    }

    @Test
    public void testLinkedBreaksConnectTracks() {
        final SignalBoxGrid grid = new SignalBoxGrid(null);
        final Point firstPoint = new Point(5, 5);
        final Point secondPoint = new Point(20, 8);
        final SignalBoxNode first = addBreak(grid, firstPoint, BREAK_NONE);
        final SignalBoxNode second = addBreak(grid, secondPoint, BREAK_180);
        first.addAndSetEntry(BREAK_NONE, PathEntryType.CONNECTED_BREAK,
                new ModeIdentifier(secondPoint, BREAK_180));
        second.addAndSetEntry(BREAK_180, PathEntryType.CONNECTED_BREAK,
                new ModeIdentifier(firstPoint, BREAK_NONE));
        first.post();
        second.post();

        assertEquals(BREAK_NONE, first.getMode(new Path(new Point(4, 5), secondPoint)));
        assertEquals(BREAK_NONE, first.getMode(new Path(secondPoint, new Point(4, 5))));
        assertEquals(BREAK_180, second.getMode(new Path(firstPoint, new Point(21, 8))));
        assertEquals(BREAK_180, second.getMode(new Path(new Point(21, 8), firstPoint)));
    }

    @Test
    public void testUnlinkedBreakHasNoConnection() {
        final SignalBoxGrid grid = new SignalBoxGrid(null);
        final SignalBoxNode node = addBreak(grid, new Point(5, 5), BREAK_NONE);
        node.post();

        assertEquals(0, node.toPathIdentifier().size());
        assertNull(node.getMode(new Path(new Point(4, 5), new Point(6, 5))));
    }
}
