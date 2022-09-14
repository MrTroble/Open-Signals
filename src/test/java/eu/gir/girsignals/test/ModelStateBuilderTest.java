package eu.gir.girsignals.test;

import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.models.ModelStats;
import eu.gir.girsignals.models.TextureStats;

public class ModelStateBuilderTest {

    @Test
    public void testModelstats() {
        System.out.println(
                GIRFileReader.readallFilesfromDierectory("/assets/girsignals/modeldefinitions"));
        
        final Map<String, ModelStats> map = ModelStats
                .getfromJson("/assets/girsignals/modeldefinitions");
        map.forEach((test1, test2) -> {
            System.out.println(test1);
            test2.getModels().forEach((test3, test4) -> {
                for (int i = 0; i < test4.getTexture().size(); i++) {

                    final TextureStats test5 = test4.getTexture().get(i);
                    System.out.println("This is model: " + test3);
                    System.out.println("The Autoblockstate is: " + test5.isautoBlockstate());
                    System.out.println("The " + i + ". blockstate of " + test3 + " is: "
                            + test5.getBlockstate());
                    final Map<String, String> map1 = ModelStats
                            .createRetexture(test5.getRetextures(), test2.getTextures());
                    map1.forEach((str1, str2) -> {
                        System.out.println("Der Key ist: " + str1);
                        System.out.println("Der Value ist: " + str2);
                    });
                }
            });
        });
    }
}