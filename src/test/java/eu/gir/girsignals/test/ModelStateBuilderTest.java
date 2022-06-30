package eu.gir.girsignals.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import eu.gir.girsignals.models.ModelStateBuilder;

public class ModelStateBuilderTest {
    
    final Gson gson = new Gson();
    
    @Test
     public void modelStateBuilderTest() {
        //Map<String, JsonObject> testmap = new HashMap<>();
        Map<String, String> testmap2 = new HashMap<>();
        //testmap = ModelStateBuilder.fromJson("/assets/girsignals/modeldefinitions");
        testmap2 = ModelStateBuilder.readallFilesfromDierectory("/assets/girsignals/modeldefinitions");
        //System.out.println(testmap);
        System.out.println(testmap2);
    }
}
