package eu.gir.girsignals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.models.ModelStateBuilder;

public class ModelStateBuilderTest {

    /*private final ModelStateBuilder state;

    public ModelStateBuilderTest(final ModelStateBuilder state) {
        this.state = state;
    }
    */
    @Test
    public void modelStateBuilderTest() {

        /*
         * Map<String, JsonObject> testmap = new HashMap<>(); Map<String, String>
         * testmap2 = new HashMap<>(); // Map<String, JsonElement> testmap3 = new
         * HashMap<>(); // testmap = //
         * ModelStateBuilder.getasJsonObject("/assets/girsignals/modeldefinitions");
         * testmap2 = ModelStateBuilder
         * .readallFilesfromDierectory("/assets/girsignals/modeldefinitions"); //
         * testmap3 = state.getfromJson("/assets/girsignals/modeldefinitions");
         * System.out.println(testmap); System.out.println(testmap2); //
         * System.out.println(testmap3);
         */

        ModelStateBuilder.getfromJson("/assets/girsignals/modeldefinitions");
    }
}
