package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.handler.SignalStateFile;
import com.troblecodings.signals.handler.SignalStateFileV2;
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

    private static BlockPos getRandomBlockPos() {
        return new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(-64, 321), RANDOM.nextInt());
    }

    @Test
    public void serializeAndDeserializePos() {
        for (int i = 0; i < 1000; i++) {
            final BlockPos pos = getRandomBlockPos();
            final ChunkPos chunk = new ChunkPos(pos);
            final byte[] array = SignalStateFileV2.getChunkPosFromPos(chunk, pos);
            assertEquals(pos, SignalStateFileV2.getPosFromChunkPos(chunk, array));
        }
    }

    @Test
    public void creationAndAddition() {
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
    }

    @Test
    public void readAndWrite() {
        final SignalStateFileV2 file = new SignalStateFileV2(path);

        final BlockPos firstcreate = GIRSyncEntryTests.randomBlockPos();
        final SignalStatePosV2 positionInFile = file.create(firstcreate);
        final ByteBuffer buffer = ByteBuffer.allocate(SignalStateFile.STATE_BLOCK_SIZE);
        RANDOM.nextBytes(buffer.array());
        file.write(positionInFile, buffer);

        final ByteBuffer outbuffer = file.read(positionInFile);

        assertArrayEquals(buffer.array(), outbuffer.array());
    }

    @Test
    public void moreThenPossible() {
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
        for (int i = 0; i < listOfPos.size() / 1000; i++) {
            final Map.Entry<BlockPos, SignalStatePosV2> entry = listOfPos.get(i);
            final SignalStatePosV2 findPos = file.find(entry.getKey());
            assertEquals(buffer, file.read(findPos));
            assertEquals(entry.getValue(), findPos);
        }
    }

    @Test
    public void readAndWriteCritical() {
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
    }

    @Test
    public void testDelete() {
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
    }

    @Test
    public void testSpeedOfFind() {
        final SignalStateFileV2 file = new SignalStateFileV2(path);
        assertNull(file.find(getRandomBlockPos()));
    }

}
