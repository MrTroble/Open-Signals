package com.troblecodings.signals.test;

import org.junit.jupiter.api.Test;

import com.troblecodings.signals.FileReader;

public final class ModelStateBuilderTest {

    private ModelStateBuilderTest() {
    }

    @Test
    public static void testModelstats() {

        System.out.println(
                FileReader.readallFilesfromDierectory("/assets/signals/modeldefinitions"));
    }
}