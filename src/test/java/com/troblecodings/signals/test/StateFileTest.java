package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    public void creationAndAddition() {
        final SignalStateFile file = new SignalStateFile(path);
        final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePos createPos = file.create(firstcreate);
        assertFalse(createPos.offset < SignalStateFile.MAX_OFFSET_OF_INDEX);
        assertNotNull(createPos);

        final SignalStatePos position = file.find(firstcreate);
        assertNotNull(position);
        assertEquals(position, createPos);

        final SignalStateFile file2 = new SignalStateFile(path);
        final SignalStatePos position2 = file2.find(firstcreate);
        assertNotNull(position2);
        assertEquals(position2, createPos);
    }

    @Test
    public void readAndWrite() {
        final SignalStateFile file = new SignalStateFile(path);

        final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePos positionInFile = file.create(firstcreate);
        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        GIRSyncEntryTests.RANDOM.nextBytes(buffer.array());
        file.write(positionInFile, buffer);

        final ByteBuffer outbuffer = file.read(positionInFile);

        assertArrayEquals(buffer.array(), outbuffer.array());
    }

    @Test
    public void moreThenPossible() {
        final SignalStateFile file = new SignalStateFile(path);
        final List<Map.Entry<BlockPos, SignalStatePos>> listOfPos = new ArrayList<>();
        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        for (int i = 0; i < 5000; i++) {
            final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
            final SignalStatePos statePos = file.create(firstcreate);
            file.write(statePos, buffer);
            listOfPos.add(Map.entry(firstcreate, statePos));
        }
        for (int i = 0; i < listOfPos.size() / 1000; i++) {
            final Map.Entry<BlockPos, SignalStatePos> entry = listOfPos.get(i);
            final SignalStatePos findPos = file.find(entry.getKey());
            assertEquals(buffer, file.read(findPos));
            assertEquals(entry.getValue(), findPos);
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

    @Test
    public void testDelete() {
        final SignalStateFile file = new SignalStateFile(path);
        final BlockPos first = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePos posInFile = file.create(first);

        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        file.write(posInFile, buffer);

        final SignalStatePos posToFind = file.find(first);

        assertEquals(posInFile, posToFind);
        file.deleteIndex(first);
        assertNull(file.find(first));

        final SignalStatePos secondPos = file.create(first);
        file.write(secondPos, buffer);

        final SignalStatePos secondPosToFind = file.find(first);

        assertEquals(secondPos, secondPosToFind);
        file.deleteIndex(first);
        assertNull(file.find(first));
    }

    @Test
    public void testGetAllEntries() {
        final SignalStateFile file = new SignalStateFile(path);
        final Map<BlockPos, ByteBuffer> map = new HashMap<>();
        for (int i = 0; i < SignalStateFile.MAX_ELEMENTS_PER_FILE + 100; i++) {
            final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
            GIRSyncEntryTests.RANDOM.nextBytes(buffer.array());
            final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
            final SignalStatePos statePos = file.create(firstcreate);
            file.write(statePos, buffer);
            map.put(firstcreate, buffer);
        }
        final Map<BlockPos, ByteBuffer> readOutEntries = file.getAllEntries();
        assertEquals(map.size(), readOutEntries.size());
        map.forEach((pos, byteBuffer) -> {
            assertTrue(readOutEntries.containsKey(pos));
            assertEquals(byteBuffer, readOutEntries.get(pos));
        });
    }
}