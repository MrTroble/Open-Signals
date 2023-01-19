package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.troblecodings.signals.statehandler.SignalStateFile;
import com.troblecodings.signals.statehandler.SignalStatePos;

import net.minecraft.core.BlockPos;

public class StateFileTest {

    @Test
    public void creationAndAddition() throws IOException {
        Path path = Paths.get("test/statefiles");
        if (Files.exists(path)) {
            Files.list(path).forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
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
