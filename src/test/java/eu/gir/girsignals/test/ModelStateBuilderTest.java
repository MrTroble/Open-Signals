package eu.gir.girsignals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;

public class ModelStateBuilderTest {

    @Test
    public void testModelstats() {
        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
    }
}