package eu.gir.girsignals.test;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.models.Texture;

public class ModelStateBuilderTest {

    private Texture texture = new Texture();

    @Test
    public void testModelstats() {
        texture.getPredicates();
    }

    /*private void test() {
        Map<String, String> map = state.getfromJson("/assets/girsignals/modeldefinitions");
        System.out.println(map);
        map.forEach((filename, classes) -> {
            System.out.println(filename.equals(classes));
        });
    }*/
}
