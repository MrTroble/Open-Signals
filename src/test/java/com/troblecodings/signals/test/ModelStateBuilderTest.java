package com.troblecodings.signals.test;

import org.junit.jupiter.api.Test;

import com.troblecodings.signals.GIRFileReader;

public final class ModelStateBuilderTest {

    private ModelStateBuilderTest() {
    }

    @Test
    public static void testModelstats() {

        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
    }
}