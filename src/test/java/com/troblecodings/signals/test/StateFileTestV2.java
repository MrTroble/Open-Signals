package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.handler.SignalStateFile;
import com.troblecodings.signals.handler.SignalStateFileV2;
import com.troblecodings.signals.handler.SignalStatePos;
import com.troblecodings.signals.handler.SignalStatePosV2;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class StateFileTestV2 {

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

    private static final Random RANDOM = new Random();

    public static BlockPos getRandomBlockPos() {
        return new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(-64, 321), RANDOM.nextInt());
    }

    @Test
    public void serializeAndDeserializePos() {
        System.out.println("Started serializeDeserialize test for SignalStateFile!");
        for (int i = 0; i < 1000; i++) {
            final BlockPos pos = getRandomBlockPos();
            final ChunkPos chunk = new ChunkPos(pos);
            final byte[] array = SignalStateFileV2.getChunkPosFromPos(chunk, pos);
            assertEquals(pos, SignalStateFileV2.getPosFromChunkPos(chunk, array));
        }
        System.out.println("Finished serializeDeserialize test for SignalStateFile!");
    }

    @Test
    public void creationAndAddition() {
        System.out.println("Started creationAddition test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);
        final BlockPos firstcreate = getRandomBlockPos();
        final SignalStatePosV2 createPos = file.create(firstcreate);
        assertNotNull(createPos);

        final SignalStatePosV2 position = file.find(firstcreate);
        assertNotNull(position);
        assertEquals(position, createPos);

        final SignalStateFileV2 file2 = new SignalStateFileV2(path);
        file2.create(firstcreate);
        final SignalStatePosV2 position2 = file2.find(firstcreate);
        assertNotNull(position2);
        assertEquals(position2, createPos);
        System.out.println("Finished creationAddition test for SignalStateFile!");
    }

    @Test
    public void readAndWrite() {
        System.out.println("Started readWrite test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);

        final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePosV2 positionInFile = file.create(firstcreate);
        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        RANDOM.nextBytes(buffer.array());
        file.write(positionInFile, buffer);

        final ByteBuffer outbuffer = file.read(positionInFile);

        assertArrayEquals(buffer.array(), outbuffer.array());
        System.out.println("Finished readWrite test for SignalStateFile!");
    }

    @Test
    public void moreThenPossible() {
        System.out.println("Started moreThenPossible test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);
        final List<Map.Entry<BlockPos, SignalStatePosV2>> listOfPos = new ArrayList<>();
        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        for (int i = 0; i < 5000; i++) {
            final BlockPos firstcreate = getRandomBlockPos();
            final SignalStatePosV2 statePos = file.create(firstcreate);
            file.write(statePos, buffer);
            listOfPos.add(Map.entry(firstcreate, statePos));
        }
        for (int i = 0; i < listOfPos.size(); i++) {
            final Map.Entry<BlockPos, SignalStatePosV2> entry = listOfPos.get(i);
            final SignalStatePosV2 findPos = file.find(entry.getKey());
            assertEquals(buffer, file.read(findPos));
            assertEquals(entry.getValue(), findPos);
        }
        System.out.println("Finished moreThenPossible test for SignalStateFile!");
    }

    @Test
    public void readAndWriteCritical() {
        System.out.println("Started readWriteCritical test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);

        final BlockPos firstcreate = getRandomBlockPos();
        final SignalStatePosV2 positionInFile = file.create(firstcreate);

        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFileV2.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        file.write(positionInFile, buffer);

        final BlockPos secondCreate = getRandomBlockPos();
        final SignalStatePosV2 secondpositionInFile = file.create(secondCreate);
        file.write(secondpositionInFile, buffer);

        final ByteBuffer outbuffer = file.read(positionInFile);
        assertArrayEquals(buffer.array(), outbuffer.array());

        final ByteBuffer outbuffer2 = file.read(secondpositionInFile);
        assertArrayEquals(buffer.array(), outbuffer2.array());
        System.out.println("Finished readWriteCritical test for SignalStateFile!");
    }

    @Test
    public void testDelete() {
        System.out.println("Started Delete test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);
        final BlockPos first = getRandomBlockPos();
        final SignalStatePosV2 posInFile = file.create(first);

        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFileV2.STATE_BLOCK_SIZE);
        buffer.array()[0] = (byte) 0xFF;
        buffer.array()[255] = (byte) 0x0F;
        file.write(posInFile, buffer);

        final SignalStatePosV2 posToFind = file.find(first);

        assertEquals(posInFile, posToFind);
        file.deleteIndex(first);
        assertNull(file.find(first));

        final SignalStatePosV2 secondPos = file.create(first);
        file.write(secondPos, buffer);

        final SignalStatePosV2 secondPosToFind = file.find(first);

        assertEquals(secondPos, secondPosToFind);
        file.deleteIndex(first);
        assertNull(file.find(first));
        System.out.println("Finished Delete test for SignalStateFile!");
    }

    @Test
    public void testSpeedOfFind() {
        System.out.println("Started SpeedOfFind test for SignalStateFile!");
        final SignalStateFileV2 file = new SignalStateFileV2(path);
        assertNull(file.find(getRandomBlockPos()));
        System.out.println("Finished SpeedOfFind test for SignalStateFile!");
    }

    @Test
    public void testMigration() {
        System.out.println("Started Migration test for SignalStateFile!");
        final SignalStateFile file = new SignalStateFile(path);
        final Map<BlockPos, ByteBuffer> map = new HashMap<>();
        for (int i = 0; i < SignalStateFile.MAX_ELEMENTS_PER_FILE + 100; i++) {
            final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
            RANDOM.nextBytes(buffer.array());
            final BlockPos firstcreate = getRandomBlockPos();
            final SignalStatePos statePos = file.create(firstcreate);
            file.write(statePos, buffer);
            map.put(firstcreate, buffer);
        }
        final Map<BlockPos, ByteBuffer> contentMap = file.getAllEntries();
        final SignalStateFileV2 fileV2 = new SignalStateFileV2(path);
        assertEquals(map.size(), contentMap.size());
        contentMap.forEach((pos, buffer) -> {
            final SignalStatePosV2 statePos = fileV2.create(pos);
            fileV2.write(statePos, buffer);
        });
        contentMap.forEach((pos, buffer) -> {
            final SignalStatePos posOldFile = file.find(pos);
            final SignalStatePosV2 posInNewFile = fileV2.find(pos);
            assertTrue(posOldFile != null);
            assertTrue(posInNewFile != null);
            assertEquals(file.read(posOldFile), fileV2.read(posInNewFile));
        });
        System.out.println("Finished Migration test for SignalStateFile!");
    }
}