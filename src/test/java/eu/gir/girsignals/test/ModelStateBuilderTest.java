package eu.gir.girsignals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.FileReader;

public class ModelStateBuilderTest {

    @Test
    public void testModelstats() {
        System.out.println(FileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
    }
}
