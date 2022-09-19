package eu.gir.girsignals.test;

import eu.gir.girsignals.GIRFileReader;

public class ModelStateBuilderTest {

    public static void testModelstats() {

        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
    }
}