package com.troblecodings.signals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.utils.FileReader;

public final class ModelStateBuilderTest {

    private ModelStateBuilderTest() {
    }

    @Test
    public static void testModelstats() {

        System.out.println(
                FileReader.readallFilesfromDierectory("/assets/signals/modeldefinitions"));
    }
}