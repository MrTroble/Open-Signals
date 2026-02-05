package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.debug.DebugNetworkHandler;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.IPathEntry;
import com.troblecodings.signals.signalbox.entrys.ModeIdentifierEntry;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;
import com.troblecodings.signals.signalbox.entrys.PointEntry;

import net.minecraft.world.level.block.Rotation;

public class SignalBoxNetworkTest {

    private static final Random RANDOM = new Random();

    private SignalBoxGrid grid = new SignalBoxGrid(null);
    private DebugNetworkHandler handler = new DebugNetworkHandler(grid);

    @BeforeAll
    public static void setUpFactory() {
        SignalBoxFactory.setUpFactoryForTests();
    }

    @Test
    public void testAddAndRemoveMode() {
        final Map<Point, ModeSet> modes = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            Point point = getRandPoint();
            while (modes.containsKey(point)) {
                point = getRandPoint();
            }
            final ModeSet mode = getRandModeSet();
            handler.sendModeAdd(new ModeIdentifier(point, mode));
            modes.put(point, mode);
        }
        modes.forEach((point, mode) -> {
            assertTrue(grid.getNode(point).getModes().keySet().contains(mode));
            handler.sendModeRemove(new ModeIdentifier(point, mode));
            assertTrue(!grid.getNode(point).getModes().keySet().contains(mode));
        });
    }

    @Test
    public void testCounter() {
        final int counter = RANDOM.nextInt(0, 1000);
        grid.setCounter(counter);
        handler.sendCounter();
        assertEquals(counter, grid.getCurrentCounter());
    }

    @SuppressWarnings("unchecked")
    @Test
    public <T> void testAddAndRemoveEntry() {
        final Map<ModeIdentifier, Map.Entry<PathOptionEntry, PathEntryType<?>>> entries =
                new HashMap<>();
        for (int i = 0; i < 500; i++) {
            final ModeSet mode = getRandModeSet();
            ModeIdentifier modeIdent = new ModeIdentifier(getRandPoint(), mode);
            while (entries.containsKey(modeIdent)) {
                modeIdent = new ModeIdentifier(getRandPoint(), mode);
            }
            final PathOptionEntry entry = new PathOptionEntry();
            final PathEntryType<T> entryType = (PathEntryType<T>) getRandEntryType();
            final IPathEntry<T> iPathEntry = entryType.newValue();
            iPathEntry.setValue(iPathEntry.getDefaultValue());
            entry.addEntry(entryType, iPathEntry);
            handler.sendEntryAdd(modeIdent, entryType, iPathEntry);
            entries.put(modeIdent, Maps.immutableEntry(entry, entryType));
        }
        entries.forEach((modeIdent, entry) -> {
            assertTrue(grid.getNode(modeIdent.point).getOption(modeIdent.mode).get()
                    .equals(entry.getKey()));
            handler.sendEntryRemove(modeIdent, entry.getValue());
            assertTrue(!grid.getNode(modeIdent.point).getOption(modeIdent.mode).get()
                    .equals(entry.getKey()));
        });
    }

    @Test
    public void testNodeLabel() {
        final Map<Point, String> labels = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            Point point = getRandPoint();
            while (labels.containsKey(point)) {
                point = getRandPoint();
            }
            final ModeSet mode = getRandModeSet();
            handler.sendModeAdd(new ModeIdentifier(point, mode));
            handler.sendNodeLabel(point, "Test");
            labels.put(point, "Test");
        }
        labels.forEach(
                (point, label) -> assertTrue(grid.getNode(point).getCustomText().equals(label)));
    }

    @Test
    public void testAutoPoint() {
        final List<Point> points = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Point point = getRandPoint();
            while (points.contains(point)) {
                point = getRandPoint();
            }
            final ModeSet mode = getRandModeSet();
            handler.sendModeAdd(new ModeIdentifier(point, mode));
            handler.sendAutoPoint(point, true);
            points.add(point);
        }
        points.forEach(point -> assertTrue(grid.getNode(point).isAutoPoint()));
    }

    @Test
    public void testManuellOutput() {
        final List<ModeIdentifier> points = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Point point = getRandPoint();
            final ModeSet mode = getRandModeSet();
            final ModeIdentifier ident = new ModeIdentifier(point, mode);
            while (points.contains(ident)) {
                point = getRandPoint();
            }
            handler.sendModeAdd(ident);
            handler.sendManuellOutputAdd(point, mode);
            points.add(ident);
        }
        points.forEach(ident -> {
            assertTrue(grid.getNode(ident.point).containsManuellOutput(ident.mode));
            handler.sendManuellOutputRemove(ident.point, ident.mode);
            assertTrue(!grid.getNode(ident.point).containsManuellOutput(ident.mode));
        });
    }

    @BeforeEach
    public void initializeNewGridAndNetwork() {
        grid = new SignalBoxGrid(null);
        handler = new DebugNetworkHandler(grid);
    }

    private Point getRandPoint() {
        return new Point(RANDOM.nextInt(0, 101), RANDOM.nextInt(0, 101));
    }

    private ModeSet getRandModeSet() {
        return new ModeSet(EnumGuiMode.values()[RANDOM.nextInt(0, EnumGuiMode.values().length)],
                Rotation.values()[RANDOM.nextInt(0, Rotation.values().length)]);
    }

    private static final List<PathEntryType<?>> PATHENTRY_TYPES =
            Lists.newArrayList(PathEntryType.ALL_ENTRIES).stream()
                    .filter(entry -> !(entry.getEntryClass().equals(ModeIdentifierEntry.class)
                            || entry.getEntryClass().equals(PointEntry.class)))
                    .collect(Collectors.toList());

    private PathEntryType<?> getRandEntryType() {
        return PATHENTRY_TYPES.get(RANDOM.nextInt(0, PATHENTRY_TYPES.size()));
    }

}
