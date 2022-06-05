package eu.gir.girsignals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.signalbox.ModeSet;
import eu.gir.girsignals.signalbox.Path;
import eu.gir.girsignals.signalbox.Point;
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

    public static void testISavable(final ISaveable toSave, final ISaveable fresh) {
        final NBTTagCompound compound = new NBTTagCompound();
        toSave.write(compound);
        assertNotEquals(toSave, fresh);
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
        testISavable(point1, new Point(0, 0));

        final Point point2 = new Point(RANDOM.nextInt(), RANDOM.nextInt());
        testISavable(new Path(point1, point2), new Path(new Point(0, 0), new Point(0, 0)));

        final ModeSet testSet = new ModeSet(randomEnum(EnumGuiMode.class),
                randomEnum(Rotation.class));
        testISavable(testSet, new ModeSet(EnumGuiMode.BUE, Rotation.NONE));

        final PathOptionEntry entry = new PathOptionEntry();
        entry.setEntry(PathEntryType.SPEED, RANDOM.nextInt());
        final EnumPathUsage oldUsage = randomEnum(EnumPathUsage.class);
        entry.setEntry(PathEntryType.PATHUSAGE, oldUsage);

        final PathOptionEntry copyEntry = new PathOptionEntry();
        testISavable(entry, copyEntry);

        final PathOptionEntry newEntry = new PathOptionEntry();
        newEntry.setEntry(PathEntryType.PATHUSAGE, oldUsage);
        entry.setEntry(PathEntryType.SPEED, null);
        assertEquals(newEntry, entry);

        assertThrowsExactly(NullPointerException.class, () -> new ModeSet(null, null));
        assertThrowsExactly(NullPointerException.class, () -> new ModeSet(null));
        assertThrowsExactly(NullPointerException.class, () -> new Path(null, null));
    }

}
