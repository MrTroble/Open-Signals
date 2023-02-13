package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.handler.SignalStateFile;
import com.troblecodings.signals.handler.SignalStatePos;

import net.minecraft.core.BlockPos;

public class StateFileTest {

    private static Path path = null;

    @BeforeEach
    public void reset() throws IOException {
        path = Paths.get("test/statefiles");
        if (Files.exists(path)) {
            Files.list(path).forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (IOException e) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Test
    public void creationAndAddition() {
        SignalStateFile file = new SignalStateFile(path);
        BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        SignalStatePos createPos = file.create(firstcreate);
        assertFalse(createPos.offset < SignalStateFile.MAX_OFFSET_OF_INDEX);
        assertNotNull(createPos);

        SignalStatePos position = file.find(firstcreate);
        assertNotNull(position);
        assertEquals(position, createPos);

        SignalStateFile file2 = new SignalStateFile(path);
        SignalStatePos position2 = file2.find(firstcreate);
        assertNotNull(position2);
        assertEquals(position2, createPos);
    }

    @Test
    public void readAndWrite() {
        SignalStateFile file = new SignalStateFile(path);

        BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        SignalStatePos positionInFile = file.create(firstcreate);
        ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        GIRSyncEntryTests.RANDOM.nextBytes(buffer.array());
        file.write(positionInFile, buffer);

        ByteBuffer outbuffer = file.read(positionInFile);

        assertArrayEquals(buffer.array(), outbuffer.array());
    }

    @Test
    public void moreThenPossible() {
        SignalStateFile file = new SignalStateFile(path);
        List<Map.Entry<BlockPos, SignalStatePos>> listOfPos = new ArrayList<>();
        for (int i = 0; i < SignalStateFile.MAX_ELEMENTS_PER_FILE + 10; i++) {
            BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
            listOfPos.add(Map.entry(firstcreate, file.create(firstcreate)));
        }
        for (int i = 0; i < listOfPos.size() / 1000; i++) {
            Map.Entry<BlockPos, SignalStatePos> entry = listOfPos.get(i);
            assertEquals(entry.getValue(), file.find(entry.getKey()));
        }
    }

    @Test
    public void readAndWriteCritical() {
        final SignalStateFile file = new SignalStateFile(path);

        final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePos positionInFile = file.create(firstcreate);

        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        file.write(positionInFile, buffer);

        final BlockPos secondCreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePos secondpositionInFile = file.create(secondCreate);
        file.write(secondpositionInFile, buffer);

        final ByteBuffer outbuffer = file.read(positionInFile);
        assertArrayEquals(buffer.array(), outbuffer.array());

        final ByteBuffer outbuffer2 = file.read(secondpositionInFile);
        assertArrayEquals(buffer.array(), outbuffer2.array());
    }

}
