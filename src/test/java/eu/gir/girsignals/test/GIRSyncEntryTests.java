package eu.gir.girsignals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.ModeSet;
import eu.gir.girsignals.signalbox.Path;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.SignalBoxUtil;
import eu.gir.girsignals.signalbox.entrys.IPathEntry;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
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
        ISaveable fresh;
        do {
            fresh = getter.get();
        } while (toSave.equals(fresh));
        fresh.read(compound);
        assertEquals(toSave, fresh);
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
        assertThrowsExactly(NullPointerException.class, () -> new SignalBoxNode(null));
    }

    @Test
    public void testSignalNode() {
        final ModeSet testSet = new ModeSet(randomEnum(EnumGuiMode.class),
                randomEnum(Rotation.class));
        final Point point2 = new Point(RANDOM.nextInt(), RANDOM.nextInt());
        final Point point1 = new Point(RANDOM.nextInt(), RANDOM.nextInt());

        final SignalBoxNode signalBoxNode = new SignalBoxNode(point2);
        assertFalse(signalBoxNode.has(testSet));
        signalBoxNode.add(testSet);
        assertTrue(signalBoxNode.has(testSet));
        signalBoxNode.remove(testSet);
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
        assertTrue(signalBoxNode.getOption(new Path(p1, p2)).isPresent());
        assertTrue(finalNode.getOption(new Path(p1, p2)).isPresent());

        assertFalse(finalNode.getOption(new Path(p1.delta(new Point(21212, RANDOM.nextInt())), p2))
                .isPresent());

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
    }
}
