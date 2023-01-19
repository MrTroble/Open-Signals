package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.statehandler.SignalStateFile;
import com.troblecodings.signals.statehandler.SignalStatePos;

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
    }

}
