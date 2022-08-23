package eu.gir.girsignals.test;

import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.models.GIRCustomModelLoader;
import eu.gir.girsignals.models.ModelStats;

public class ModelStateBuilderTest {

    @Test
    public void testModelstats() {
        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
        
        final Map<String, ModelStats> map = GIRCustomModelLoader.test();
        System.out.println(map);
        for (Map.Entry<String, ModelStats> entry : map.entrySet()) {
            String key = entry.getKey();
            System.out.println(key);
            ModelStats val = entry.getValue();
            System.out.println(val);
        }
    }
}
