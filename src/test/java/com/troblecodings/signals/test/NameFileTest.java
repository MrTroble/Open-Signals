package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.handler.NameHandlerFile;
import com.troblecodings.signals.handler.SignalStateFile;
import com.troblecodings.signals.handler.SignalStatePos;

import net.minecraft.util.math.BlockPos;

public class NameFileTest {

    private static Path path = null;

    @BeforeEach
    public void reset() throws IOException {
        path = Paths.get("test/statefiles");
        if (Files.exists(path)) {
            Files.list(path).forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @AfterAll
    public static void resetAll() throws IOException {
        if (Files.exists(path)) {
            Files.list(path).forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void testWriteAndRead() {
        final BlockPos pos = GIRSyncEntryTests.randomBlockPos();
        final String name = "wdasdfdgsddfwadsdf";
        final NameHandlerFile file = new NameHandlerFile(path);
        final SignalStatePos statePos = file.createState(pos, name);

        final String nameInFile = file.getString(statePos);
        assertEquals(name, nameInFile);

        assertEquals(statePos, file.find(pos));

        file.deleteIndex(pos);

        assertNull(file.find(pos));
        final SignalStatePos statePos2 = file.createState(pos, name);
        final String nameInFile2 = file.getString(statePos2);
        assertEquals(name, nameInFile2);

        assertEquals(statePos2, file.find(pos));
    }

    @Test
    public void testException() {
        final NameHandlerFile file = new NameHandlerFile(path);
        String str = "";
        for (int i = 0; i < 129; i++) {
            str += "A";
        }
        final String s = str;
        assertThrowsExactly(IllegalArgumentException.class,
                () -> file.createState(GIRSyncEntryTests.randomBlockPos(), s));
    }

    @Test
    public void moreThanPossible() {
        final NameHandlerFile file = new NameHandlerFile(path);
        final Map<BlockPos, String> allNames = new HashMap<>();
        final List<Map.Entry<BlockPos, SignalStatePos>> listOfPos = new ArrayList<>();
        String testString = "";
        for (int i = 0; i < SignalStateFile.MAX_ELEMENTS_PER_FILE + 10; i++) {
            testString = "test_" + String.valueOf(i);
            final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
            final SignalStatePos pos = file.createState(firstcreate, testString);
            listOfPos.add(Map.entry(firstcreate, pos));
            allNames.put(firstcreate, testString);
        }
        for (int i = 0; i < listOfPos.size() / 1000; i++) {
            final Map.Entry<BlockPos, SignalStatePos> entry = listOfPos.get(i);
            assertEquals(entry.getValue(), file.find(entry.getKey()));
            assertEquals(allNames.get(entry.getKey()), file.getString(entry.getValue()));
        }
    }
}
