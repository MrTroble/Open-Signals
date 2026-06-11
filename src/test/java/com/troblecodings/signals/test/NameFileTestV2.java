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

import com.troblecodings.signals.handler.NameHandlerFileV2;
import com.troblecodings.signals.handler.SignalStatePosV2;

import net.minecraft.core.BlockPos;

public class NameFileTestV2 {

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
        final BlockPos pos = StateFileTestV2.getRandomBlockPos();
        final String name = "wdasdfdgsddfwadsdf";
        final NameHandlerFileV2 file = new NameHandlerFileV2(path);
        final SignalStatePosV2 statePos = file.createState(pos, name);

        final String nameInFile = file.getString(statePos);
        assertEquals(name, nameInFile);

        assertEquals(statePos, file.find(pos));

        file.deleteIndex(pos);

        assertNull(file.find(pos));
        final SignalStatePosV2 statePos2 = file.createState(pos, name);
        final String nameInFile2 = file.getString(statePos2);
        assertEquals(name, nameInFile2);

        assertEquals(statePos2, file.find(pos));
    }

    @Test
    public void testException() {
        final NameHandlerFileV2 file = new NameHandlerFileV2(path);
        String str = "";
        for (int i = 0; i < 129; i++) {
            str += "A";
        }
        final String s = str;
        assertThrowsExactly(IllegalArgumentException.class,
                () -> file.createState(StateFileTestV2.getRandomBlockPos(), s));
    }

    @Test
    public void moreThanPossible() {
        final NameHandlerFileV2 file = new NameHandlerFileV2(path);
        final Map<BlockPos, String> allNames = new HashMap<>();
        final List<Map.Entry<BlockPos, SignalStatePosV2>> listOfPos = new ArrayList<>();
        String testString = "";
        for (int i = 0; i < 5000; i++) {
            testString = "test_" + String.valueOf(i);
            final BlockPos firstcreate = StateFileTestV2.getRandomBlockPos();
            final SignalStatePosV2 pos = file.createState(firstcreate, testString);
            listOfPos.add(Map.entry(firstcreate, pos));
            allNames.put(firstcreate, testString);
        }
        for (int i = 0; i < listOfPos.size(); i++) {
            final Map.Entry<BlockPos, SignalStatePosV2> entry = listOfPos.get(i);
            assertEquals(entry.getValue(), file.find(entry.getKey()));
            assertEquals(allNames.get(entry.getKey()), file.getString(entry.getValue()));
        }
    }
}
