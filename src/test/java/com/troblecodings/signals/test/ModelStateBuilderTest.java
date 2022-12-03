package com.troblecodings.signals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;

public final class ModelStateBuilderTest {

    private ModelStateBuilderTest() {
    }

    @Test
    public static void testModelstats() {

        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/signals/modeldefinitions"));
    }
}