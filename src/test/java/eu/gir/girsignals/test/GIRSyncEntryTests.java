package eu.gir.girsignals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.ModeSet;
import eu.gir.girsignals.signalbox.Path;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxGrid;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.SignalBoxPathway;
import eu.gir.girsignals.signalbox.SignalBoxUtil;
import eu.gir.girsignals.signalbox.entrys.BlockposEntry;
import eu.gir.girsignals.signalbox.entrys.BoolEntry;
import eu.gir.girsignals.signalbox.entrys.EnumEntry;
import eu.gir.girsignals.signalbox.entrys.INetworkSavable;
import eu.gir.girsignals.signalbox.entrys.IPathEntry;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import eu.gir.girsignals.signalbox.entrys.IntegerEntry;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import scala.util.Random;

public class GIRSyncEntryTests {

    private static final Random RANDOM = new Random();

    public static <T extends Enum<T>> T randomEnum(final Class<T> clazz) {
        final T[] values = clazz.getEnumConstants();
        final int id = RANDOM.nextInt(values.length);
        return values[id];
    }

    public static BlockPos randomBlockPos() {
        return new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt());
    }

    public static void testISavable(final ISaveable toSave, final Supplier<ISaveable> getter) {
        final NBTTagCompound compound = new NBTTagCompound();
        toSave.write(compound);
        final AtomicReference<ISaveable> fresh = new AtomicReference<>();
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            do {
                fresh.set(getter.get());
            } while (toSave.equals(fresh.get()));
        });
        fresh.get().read(compound);
        assertEquals(toSave, fresh.get());
    }

    @SuppressWarnings("unchecked")
    public static void testReadWriteSingle(final PathEntryType<?> entry, final Object value) {
        final IPathEntry<Object> pathEntry = (IPathEntry<Object>) entry.newValue();
        pathEntry.setValue(value);
        final NBTTagCompound compound = new NBTTagCompound();
        pathEntry.write(compound);
        final IPathEntry<Object> pathEntry2 = (IPathEntry<Object>) entry.newValue();
        pathEntry2.read(compound);
        assertEquals(pathEntry2, pathEntry);
        assertNotEquals(pathEntry2.getName(), "defaultEntry");
        assertNotEquals(pathEntry.getName(), "defaultEntry");
    }

    @Test
    public void testSpecialEntrys() {
        testReadWriteSingle(PathEntryType.BLOCKING, randomBlockPos());
        testReadWriteSingle(PathEntryType.OUTPUT, randomBlockPos());
        testReadWriteSingle(PathEntryType.RESETING, randomBlockPos());
        testReadWriteSingle(PathEntryType.SIGNAL, randomBlockPos());
        testReadWriteSingle(PathEntryType.SPEED, RANDOM.nextInt());
        testReadWriteSingle(PathEntryType.PATHUSAGE, randomEnum(EnumPathUsage.class));
    }

    @Test
    public void testSavables() {
        final Point point1 = new Point(RANDOM.nextInt(), RANDOM.nextInt());
        testISavable(point1, () -> new Point(RANDOM.nextInt(), RANDOM.nextInt()));

        final Point point2 = new Point(RANDOM.nextInt(), RANDOM.nextInt());
        testISavable(new Path(point1, point2),
                () -> new Path(new Point(RANDOM.nextInt(), RANDOM.nextInt()), new Point(0, 0)));

        final ModeSet testSet = new ModeSet(randomEnum(EnumGuiMode.class),
                randomEnum(Rotation.class));
        testISavable(testSet,
                () -> new ModeSet(randomEnum(EnumGuiMode.class), randomEnum(Rotation.class)));

        final PathOptionEntry entry = new PathOptionEntry();
        entry.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
        final EnumPathUsage oldUsage = randomEnum(EnumPathUsage.class);
        entry.setEntry(PathEntryType.PATHUSAGE, oldUsage);

        final PathOptionEntry copyEntry = new PathOptionEntry();
        testISavable(entry, () -> copyEntry);

        final PathOptionEntry newEntry = new PathOptionEntry();
        newEntry.setEntry(PathEntryType.PATHUSAGE, oldUsage);
        entry.setEntry(PathEntryType.SPEED, null);
        assertEquals(newEntry, entry);

        assertThrowsExactly(NullPointerException.class, () -> new ModeSet(null, null));
        assertThrowsExactly(NullPointerException.class, () -> new ModeSet(null));
        assertThrowsExactly(NullPointerException.class, () -> new Path(null, null));
        assertThrowsExactly(NullPointerException.class, () -> new SignalBoxNode((Point) null));
    }

    private static void testValidStartNot(final SignalBoxNode validStartCheck, final ModeSet set) {
        assertFalse(validStartCheck.isValidStart());
        validStartCheck.add(set);
        assertFalse(validStartCheck.isValidStart());
        validStartCheck.remove(set);
        assertFalse(validStartCheck.isValidStart());
    }

    private static void testValidStart(final SignalBoxNode validStartCheck, final ModeSet set) {
        assertFalse(validStartCheck.isValidStart());
        validStartCheck.add(set);
        assertTrue(validStartCheck.isValidStart());
        validStartCheck.remove(set);
        assertFalse(validStartCheck.isValidStart());
    }

    private static void testMakePath(final SignalBoxNode pathStart) {
        final Point p1 = new Point(pathStart.getPoint());
        final Point p2 = new Point(pathStart.getPoint());
        final Path p = new Path(p1, p2);
        p1.translate(0, -1);
        p2.translate(0, 1);
        assertTrue(pathStart.canMakePath(p, PathType.SHUNTING));
        assertTrue(pathStart.canMakePath(p, PathType.NORMAL));
        p1.translate(0, -1);
        p2.translate(0, 1);
        assertFalse(pathStart.canMakePath(p, PathType.SHUNTING));
        assertFalse(pathStart.canMakePath(p, PathType.NORMAL));
    }

    @Test
    public void testSignalNode() {
        final ModeSet testSet = new ModeSet(randomEnum(EnumGuiMode.class),
                randomEnum(Rotation.class));
        final Point point2 = new Point(RANDOM.nextInt(), RANDOM.nextInt());
        final Point point1 = new Point(RANDOM.nextInt(), RANDOM.nextInt());

        final SignalBoxNode signalBoxNode = new SignalBoxNode(point2);
        assertTrue(signalBoxNode.isEmpty());
        assertFalse(signalBoxNode.has(testSet));
        signalBoxNode.add(testSet);
        assertFalse(signalBoxNode.isEmpty());
        assertTrue(signalBoxNode.has(testSet));
        signalBoxNode.remove(testSet);
        assertTrue(signalBoxNode.isEmpty());
        assertFalse(signalBoxNode.has(testSet));
        signalBoxNode.add(testSet);
        testISavable(signalBoxNode, () -> new SignalBoxNode(point2));
        assertEquals(point2, signalBoxNode.getPoint());
        assertEquals(new SignalBoxNode(point2).getIdentifier(), signalBoxNode.getIdentifier());

        final Rotation rotation = randomEnum(Rotation.class);
        signalBoxNode.add(new ModeSet(EnumGuiMode.STRAIGHT, rotation));
        signalBoxNode.post();

        final SignalBoxNode finalNode = new SignalBoxNode(point2);
        testISavable(signalBoxNode, () -> finalNode);

        assertEquals(new Point(2, 1), SignalBoxUtil.getOffset(Rotation.NONE, new Point(1, 1)));
        assertEquals(new Point(0, 1),
                SignalBoxUtil.getOffset(Rotation.CLOCKWISE_180, new Point(1, 1)));
        assertEquals(new Point(1, 2),
                SignalBoxUtil.getOffset(Rotation.CLOCKWISE_90, new Point(1, 1)));
        assertEquals(new Point(1, 0),
                SignalBoxUtil.getOffset(Rotation.COUNTERCLOCKWISE_90, new Point(1, 1)));

        final Point p1 = SignalBoxUtil.getOffset(rotation, point2);
        final Point p2 = SignalBoxUtil.getOffset(rotation.add(Rotation.CLOCKWISE_180), point2);
        final Path path = new Path(p1, p2);
        assertTrue(signalBoxNode.getOption(path).isPresent());
        assertTrue(finalNode.getOption(new Path(p1, p2)).isPresent());
        assertTrue(signalBoxNode.connections().contains(path));
        assertTrue(signalBoxNode.connections().contains(path.getInverse()));

        assertFalse(finalNode.getOption(new Path(p1.delta(new Point(21212, RANDOM.nextInt())), p2))
                .isPresent());
        assertFalse(signalBoxNode.connections()
                .contains(new Path(p1.delta(new Point(21212, RANDOM.nextInt())), p2)));

        final SignalBoxNode currentNode = new SignalBoxNode(point2);
        final SignalBoxNode nextNode = new SignalBoxNode(point1);
        assertEquals(PathType.NONE, nextNode.getPathType(new SignalBoxNode(point1)));
        assertEquals(PathType.NONE, currentNode.getPathType(nextNode));
        assertEquals(PathType.NONE, currentNode.getPathType(null));

        final ModeSet cHPMode = new ModeSet(EnumGuiMode.HP, randomEnum(Rotation.class));
        currentNode.add(cHPMode);
        assertEquals(PathType.NONE, currentNode.getPathType(nextNode));

        final ModeSet rsMode = new ModeSet(EnumGuiMode.RS, randomEnum(Rotation.class));
        nextNode.add(rsMode);
        assertEquals(PathType.NONE, currentNode.getPathType(nextNode));

        final ModeSet hpMode = new ModeSet(EnumGuiMode.HP, randomEnum(Rotation.class));
        nextNode.add(hpMode);
        assertEquals(PathType.NORMAL, currentNode.getPathType(nextNode));

        final ModeSet cRSNode = new ModeSet(EnumGuiMode.RS, randomEnum(Rotation.class));
        currentNode.add(cRSNode);
        assertEquals(PathType.NORMAL, currentNode.getPathType(nextNode));

        currentNode.remove(cHPMode);
        assertEquals(PathType.SHUNTING, currentNode.getPathType(nextNode));

        nextNode.remove(hpMode);
        assertEquals(PathType.SHUNTING, currentNode.getPathType(nextNode));

        currentNode.remove(cRSNode);
        assertEquals(PathType.NONE, currentNode.getPathType(nextNode));

        final SignalBoxNode validStartCheck = new SignalBoxNode(point1);
        for (final EnumGuiMode mode : EnumGuiMode.values()) {
            if (mode.equals(EnumGuiMode.RA10) || mode.equals(EnumGuiMode.RS)
                    || mode.equals(EnumGuiMode.HP) || mode.equals(EnumGuiMode.END)) {
                testValidStart(validStartCheck, new ModeSet(mode, randomEnum(Rotation.class)));
                continue;
            }
            testValidStartNot(validStartCheck, new ModeSet(mode, randomEnum(Rotation.class)));
        }

        final SignalBoxNode pathStart = new SignalBoxNode(new Point(0, 0));
        pathStart.add(new ModeSet(EnumGuiMode.STRAIGHT, Rotation.CLOCKWISE_90));
        pathStart.post();
        final SignalBoxNode pathMiddle = new SignalBoxNode(new Point(0, 1));
        pathMiddle.add(new ModeSet(EnumGuiMode.STRAIGHT, Rotation.COUNTERCLOCKWISE_90));
        pathMiddle.post();
        final SignalBoxNode pathEnd = new SignalBoxNode(new Point(0, 2));
        pathEnd.add(new ModeSet(EnumGuiMode.STRAIGHT, Rotation.CLOCKWISE_90));
        pathEnd.post();
        testMakePath(pathStart);
        testMakePath(pathMiddle);
        testMakePath(pathEnd);
    }

    public SignalBoxNode generateNode(final Point point) {
        final SignalBoxNode node = new SignalBoxNode(point);
        for (final Rotation rotation : Rotation.values()) {
            node.add(new ModeSet(EnumGuiMode.STRAIGHT, rotation));
            node.add(new ModeSet(EnumGuiMode.CORNER, rotation));
        }
        node.post();
        return node;
    }

    public Map<Point, SignalBoxNode> generateMap(final int pX, final int pY) {
        final Map<Point, SignalBoxNode> map = new HashMap<>();
        for (int x = 0; x < pX; x++) {
            for (int y = 0; y < pY; y++) {
                final Point point = new Point(x, y);
                map.put(point, generateNode(point));
            }
        }
        return map;
    }

    public static <T extends INetworkSavable> void testINetworkSavable(final T savable,
            final Supplier<T> supplier, final Consumer<T> consumer) {
        final AtomicReference<T> atomic = new AtomicReference<>();
        testISavable(savable, () -> atomic.updateAndGet(old -> supplier.get()));
        final NBTTagCompound network1 = new NBTTagCompound();
        savable.writeEntryNetwork(network1, false);
        final T test = atomic.get();
        assertEquals(savable, test);
        test.readEntryNetwork(network1);
        assertEquals(savable, test);
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
            do {
                consumer.accept(savable);
            } while (savable.equals(test));
        });
        final NBTTagCompound network2 = new NBTTagCompound();
        savable.writeEntryNetwork(network2, false);
        test.readEntryNetwork(network2);
        assertEquals(savable, test);
    }

    @Test
    public void testNetworkSavable() {
        final BlockposEntry bpe = new BlockposEntry();
        bpe.setValue(new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt()));
        testINetworkSavable(bpe, () -> new BlockposEntry(), entry -> entry
                .setValue(new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt())));

        final IntegerEntry ientry = new IntegerEntry();
        ientry.setValue(RANDOM.nextInt());
        testINetworkSavable(ientry, () -> new IntegerEntry(),
                entry -> entry.setValue(RANDOM.nextInt()));

        final EnumEntry<EnumGuiMode> eentry = new EnumEntry<>(EnumGuiMode.class);
        eentry.setValue(randomEnum(EnumGuiMode.class));
        testINetworkSavable(eentry, () -> new EnumEntry<>(EnumGuiMode.class),
                entry -> entry.setValue(randomEnum(EnumGuiMode.class)));

        final BoolEntry bentry = new BoolEntry();
        bentry.setValue(true);
        testINetworkSavable(bentry, () -> new BoolEntry(),
                entry -> entry.setValue(RANDOM.nextBoolean()));

        final PathOptionEntry entry = new PathOptionEntry();
        entry.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
        testINetworkSavable(entry, () -> new PathOptionEntry(),
                e -> e.setEntry(PathEntryType.PATHUSAGE, randomEnum(EnumPathUsage.class)));

        final SignalBoxNode node = new SignalBoxNode(new Point(RANDOM.nextInt(), RANDOM.nextInt()));
        ModeSet testModeImpl = null;
        for (int i = 0; i < 10; i++) {
            final ModeSet mode = new ModeSet(randomEnum(EnumGuiMode.class),
                    randomEnum(Rotation.class));
            testModeImpl = mode;
            node.add(mode);
            final PathOptionEntry consumer = node.getOption(mode).get();
            consumer.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
            consumer.setEntry(PathEntryType.PATHUSAGE, randomEnum(EnumPathUsage.class));
            consumer.setEntry(PathEntryType.SIGNAL,
                    new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt()));
        }
        final ModeSet testMode = testModeImpl;
        testINetworkSavable(node, () -> new SignalBoxNode(), n -> {
            for (int i = 0; i < 20; i++) {
                final ModeSet mode = new ModeSet(randomEnum(EnumGuiMode.class),
                        randomEnum(Rotation.class));
                n.add(mode);
                final PathOptionEntry consumer = n.getOption(mode).get();
                consumer.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
                consumer.setEntry(PathEntryType.PATHUSAGE, randomEnum(EnumPathUsage.class));
                consumer.setEntry(PathEntryType.SIGNAL,
                        new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt()));
            }
            final PathOptionEntry consumer = n.getOption(testMode).get();
            consumer.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
            consumer.setEntry(PathEntryType.PATHUSAGE, randomEnum(EnumPathUsage.class));
            consumer.setEntry(PathEntryType.SIGNAL,
                    new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt()));
            n.post();
        });
    }

    private Path makeWaypoints(final Map<Point, SignalBoxNode> map) {
        final Point p1 = new Point(RANDOM.nextInt(8) + 1, RANDOM.nextInt(8) + 1);
        Point p2;
        do {
            p2 = new Point(RANDOM.nextInt(8) + 1, RANDOM.nextInt(8) + 1);
        } while (p1.equals(p2));

        map.get(p2).add(new ModeSet(EnumGuiMode.HP, randomEnum(Rotation.class)));
        map.get(p1).add(new ModeSet(EnumGuiMode.HP, randomEnum(Rotation.class)));
        return new Path(p1, p2);
    }

    @Test
    public void testSignalBoxUtil() {
        final Map<Point, SignalBoxNode> map = generateMap(10, 10);
        final Path testPath = makeWaypoints(map);
        final Point p1 = testPath.point1;
        final Point p2 = testPath.point2;

        final Optional<SignalBoxPathway> opt = SignalBoxUtil.requestWay(map, p1, p2);
        assertTrue(opt.isPresent());

        final SignalBoxPathway pathway = opt.get();
        assertEquals(pathway.getFirstPoint(), p1);
        assertEquals(pathway.getLastPoint(), p2);

        final SignalBoxPathway pathwayCopy = new SignalBoxPathway(map);
        testISavable(pathway, () -> pathwayCopy);
        final NBTTagCompound testCompound = new NBTTagCompound();
        pathwayCopy.writeEntryNetwork(testCompound, false);
        assertEquals(pathway, pathwayCopy);
        pathwayCopy.readEntryNetwork(testCompound);
        assertEquals(pathway.getListOfNodes().size(), pathwayCopy.getListOfNodes().size());
        for (int i = 0; i < pathway.getListOfNodes().size(); i++) {
            assertEquals(pathway.getListOfNodes().get(i), pathway.getListOfNodes().get(i));
        }
        assertEquals(pathway, pathwayCopy);

        pathway.setPathStatus(EnumPathUsage.BLOCKED);
        assertEquals(pathway, pathwayCopy);

        final Map<Point, SignalBoxNode> map2 = new HashMap<>();
        map.forEach((p, n) -> {
            final SignalBoxNode node = new SignalBoxNode(new Point(p));
            final NBTTagCompound comp = new NBTTagCompound();
            n.write(comp);
            node.read(comp);
            map2.put(node.getPoint(), node);
        });
        testINetworkSavable(pathway, () -> new SignalBoxPathway(map2), pw -> {
            pw.setPathStatus(EnumPathUsage.FREE);
        });
    }

    private SignalBoxGrid makeGrid(final Consumer<Map<Point, SignalBoxNode>> consumer) {
        final SignalBoxGrid grid = new SignalBoxGrid(_u -> {
        });
        final Map<Point, SignalBoxNode> map = generateMap(50, 50);
        consumer.accept(map);
        final NBTTagCompound compound = new NBTTagCompound();
        map.values().forEach(node -> node.writeEntryNetwork(compound, false));
        grid.readEntryNetwork(compound);
        assertFalse(grid.isEmpty());
        return grid;
    }

    @Test
    public void testSignalBoxGrid() {
        final SignalBoxGrid grid = makeGrid(_u -> {
        });
        final NBTTagCompound compound = new NBTTagCompound();
        grid.write(compound);
        final SignalBoxGrid gridCopy = new SignalBoxGrid(_u -> {
        });
        gridCopy.read(compound);
        assertFalse(gridCopy.isEmpty());
        final List<SignalBoxNode> nodes = grid.getNodes();
        final List<SignalBoxNode> nodesCopy = gridCopy.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            final SignalBoxNode node = nodes.get(i);
            final SignalBoxNode nodeCopy = nodesCopy.get(i);
            assertEquals(node, nodeCopy);
        }
        testISavable(gridCopy, () -> new SignalBoxGrid(_u -> {
        }));

        final AtomicReference<Path> pathRef = new AtomicReference<>();

        final SignalBoxGrid wayTestGrid = makeGrid(map -> {
            pathRef.set(makeWaypoints(map));
        });
        final Point p1 = pathRef.get().point1;
        final Point p2 = pathRef.get().point2;
        assertTrue(wayTestGrid.requestWay(null, p1, p2));
        testISavable(wayTestGrid, () -> new SignalBoxGrid(_u -> {
        }));

        final SignalBoxGrid netTestGrid = makeGrid(map -> {
            pathRef.set(makeWaypoints(map));
        });
        testINetworkSavable(netTestGrid, () -> new SignalBoxGrid(_u -> {
        }), g -> g.getNodes()
                .forEach(node -> node.connections().forEach(path -> node.getOption(path).ifPresent(
                        entry -> entry.setEntry(PathEntryType.PATHUSAGE, EnumPathUsage.BLOCKED)))));

        final SignalBoxGrid removeTestGrid = makeGrid(map -> {
            pathRef.set(makeWaypoints(map));
        });
        testINetworkSavable(removeTestGrid, () -> new SignalBoxGrid(_u -> {
        }), g -> {
            g.getNodes().stream().findAny().ifPresent(node -> {
                final ArrayList<ModeSet> setRemove = Lists.newArrayList(node);
                setRemove.forEach(set -> node.remove(set));
                final NBTTagCompound network = new NBTTagCompound();
                g.writeEntryNetwork(network, false);
                g.readEntryNetwork(network);
            });
        });
    }
}
