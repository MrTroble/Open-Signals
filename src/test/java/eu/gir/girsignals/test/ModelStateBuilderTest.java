package eu.gir.girsignals.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.models.ModelStateBuilder;

public class ModelStateBuilderTest {
    
    @Test
     public void modelStateBuilderTest() {
        Map<String, String> testmap = new HashMap<String, String>();
        testmap = ModelStateBuilder.readfromFile("/assets/girsignals/modeldefinitions");
        System.out.println(testmap);
    }
}
