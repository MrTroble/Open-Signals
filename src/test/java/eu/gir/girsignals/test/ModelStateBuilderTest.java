package eu.gir.girsignals.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import eu.gir.girsignals.models.ModelStateBuilder;

public class ModelStateBuilderTest {
    
    final Gson gson = new Gson();
    
    @Test
     public void modelStateBuilderTest() {
        Map<String, String> testmap = new HashMap<String, String>();
        testmap = ModelStateBuilder.readallFilesfromDierectory("/assets/girsignals/modeldefinitions");
        System.out.println(testmap);
    }
}
