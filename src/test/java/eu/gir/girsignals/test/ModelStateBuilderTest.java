package eu.gir.girsignals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;

public final class ModelStateBuilderTest {

    private ModelStateBuilderTest() {
    }

    @Test
    public static void testModelstats() {

        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
    }
}