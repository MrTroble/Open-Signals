package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.debug.DebugNetworkHandler;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

import net.minecraft.util.Rotation;

public class SignalBoxMoveAreaTest {

    private static final ModeSet STRAIGHT = new ModeSet(EnumGuiMode.STRAIGHT, Rotation.NONE);
    private static final ModeSet IN_CONNECTION =
            new ModeSet(EnumGuiMode.IN_CONNECTION, Rotation.NONE);

    private SignalBoxGrid grid = new SignalBoxGrid(null);
    private DebugNetworkHandler handler = new DebugNetworkHandler(grid);

    @BeforeAll
    public static void setUpFactory() {
        SignalBoxFactory.setUpFactoryForTests();
    }

    @BeforeEach
    public void initializeNewGridAndNetwork() {
        grid = new SignalBoxGrid(null);
        handler = new DebugNetworkHandler(grid);
    }

    private SignalBoxNode addNode(final int x, final int y, final ModeSet mode) {
        final SignalBoxNode node = grid.getOrCreateNode(new Point(x, y));
        node.add(mode);
        return node;
    }

    private <T> void setEntry(final SignalBoxNode node, final ModeSet mode,
            final PathEntryType<T> type, final T value) {
        final IPathEntry<T> entry = type.newValue();
        entry.setValue(value);
        node.getOption(mode).get().addEntry(type, entry);
    }

    @Test
    public void testMoveAreaMovesNodesWithData() {
        final SignalBoxNode node = addNode(10, 10, STRAIGHT);
        node.setCustomText("Test");
        node.setAutoPointFromNetwork(true);
        setEntry(node, STRAIGHT, PathEntryType.SPEED, 80);
        addNode(11, 10, STRAIGHT);

        handler.sendMoveArea(new Point(11, 10), new Point(10, 10), 5, 3);

        assertTrue(grid.getNode(new Point(10, 10)) == null
                || grid.getNode(new Point(10, 10)).isEmpty());
        final SignalBoxNode moved = grid.getNode(new Point(15, 13));
        assertTrue(moved.has(STRAIGHT));
        assertEquals("Test", moved.getCustomText());
        assertTrue(moved.isAutoPoint());
        assertEquals(80, moved.getOption(STRAIGHT).get().getEntry(PathEntryType.SPEED).get());
        assertEquals(new Point(15, 13), moved.getPoint());
        assertTrue(grid.getNode(new Point(16, 13)).has(STRAIGHT));
    }

    @Test
    public void testMoveAreaOverlappingItself() {
        addNode(10, 10, STRAIGHT);
        addNode(11, 10, STRAIGHT);

        assertTrue(grid.moveArea(new Point(10, 10), new Point(11, 10), 1, 0));

        assertTrue(grid.getNode(new Point(10, 10)) == null
                || grid.getNode(new Point(10, 10)).isEmpty());
        assertTrue(grid.getNode(new Point(11, 10)).has(STRAIGHT));
        assertTrue(grid.getNode(new Point(12, 10)).has(STRAIGHT));
    }

    @Test
    public void testMoveAreaRejectsCollision() {
        addNode(10, 10, STRAIGHT);
        addNode(12, 10, STRAIGHT);

        assertFalse(grid.canMoveArea(new Point(10, 10), new Point(10, 10), 2, 0));
        assertFalse(grid.moveArea(new Point(10, 10), new Point(10, 10), 2, 0));
        assertTrue(grid.getNode(new Point(10, 10)).has(STRAIGHT));
    }

    @Test
    public void testMoveAreaRejectsOutOfGrid() {
        addNode(98, 98, STRAIGHT);
        addNode(0, 0, STRAIGHT);

        assertFalse(grid.moveArea(new Point(98, 98), new Point(98, 98), 5, 0));
        assertFalse(grid.moveArea(new Point(0, 0), new Point(0, 0), -1, 0));
        assertFalse(grid.moveArea(new Point(0, 0), new Point(0, 0), 0, 0));
        assertTrue(grid.getNode(new Point(98, 98)).has(STRAIGHT));
        assertTrue(grid.getNode(new Point(0, 0)).has(STRAIGHT));
        assertNull(grid.getNode(new Point(103, 98)));
    }

    @Test
    public void testMoveAreaUpdatesReferences() {
        final SignalBoxNode inNode = addNode(5, 5, IN_CONNECTION);
        addNode(10, 10, STRAIGHT);
        setEntry(inNode, IN_CONNECTION, PathEntryType.POINT, new Point(10, 10));
        final SignalBoxNode signal = addNode(20, 20, STRAIGHT);
        setEntry(signal, STRAIGHT, PathEntryType.PROTECTIONWAY_END, new Point(10, 10));

        assertTrue(grid.moveArea(new Point(10, 10), new Point(10, 10), 0, 4));

        final PathOptionEntry inEntry =
                grid.getNode(new Point(5, 5)).getOption(IN_CONNECTION).get();
        assertEquals(new Point(10, 14), inEntry.getEntry(PathEntryType.POINT).get());
        final PathOptionEntry signalEntry =
                grid.getNode(new Point(20, 20)).getOption(STRAIGHT).get();
        assertEquals(new Point(10, 14),
                signalEntry.getEntry(PathEntryType.PROTECTIONWAY_END).get());
    }
}
