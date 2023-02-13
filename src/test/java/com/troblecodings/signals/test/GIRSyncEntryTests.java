package com.troblecodings.signals.test;

import java.util.Random;

import net.minecraft.core.BlockPos;

public final class GIRSyncEntryTests {

    private GIRSyncEntryTests() {}
    
    public static final Random RANDOM = new Random();

    public static <T extends Enum<T>> T randomEnum(final Class<T> clazz) {
        final T[] values = clazz.getEnumConstants();
        final int id = RANDOM.nextInt(values.length);
        return values[id];
    }

    public static BlockPos randomBlockPos() {
        return new BlockPos(RANDOM.nextInt(), RANDOM.nextInt(), RANDOM.nextInt());
    }
}
