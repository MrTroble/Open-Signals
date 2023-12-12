package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        for (int i = 0; i < 999999999; i++) {
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
        assertFalse(createPos.offset < SignalStateFileV2.MAX_OFFSET_OF_INDEX);
        assertNotNull(createPos);

        final SignalStatePosV2 position = file.find(firstcreate);
        assertNotNull(position);
        assertEquals(position, createPos);

        final SignalStateFileV2 file2 = new SignalStateFileV2(path);
        final SignalStatePosV2 position2 = file2.find(firstcreate);
        assertNotNull(position2);
        assertEquals(position2, createPos);
    }

}
