package eu.gir.girsignals.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.models.ModelStats;
import net.minecraftforge.common.property.IUnlistedProperty;

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
                System.out.println(test4.getX());
                test4.getTexture().forEach(test5 -> {
                    System.out.println("This is model: " + test3);
                    System.out.println("The Autoblockstate is: " + test5.isautoBlockstate());
                    System.out.println("The Predicate<IExtendedBlockstate is: "
                            + test5.getPredicates(test1, test5));
                    final Map<String, String> map1 = ModelStats
                            .createRetexture(test5.getRetextures(), test2.getTextures());
                    map1.forEach((str1, str2) -> {
                        System.out.println("Der Key ist: " + str1);
                        System.out.println("Der Value ist: " + str2);
                    });
                    System.out.println();
                });
            });
        });
        final Field[] fields = SignalHV.class.getDeclaredFields();
        for (@SuppressWarnings("unused")
        final Field field : fields) {
            // System.out.println(field);
        }
    }

    @SuppressWarnings("rawtypes")
    public void testProperties() {
        ArrayList<IUnlistedProperty> signalProperties = new ArrayList<>();
        signalProperties.clear();
        if (!this.getClass().equals(Signal.class)) {
            for (final Field f : SignalHV.class.getDeclaredFields()) {
                final int mods = f.getModifiers();
                if (Modifier.isFinal(mods) && Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
                    try {
                        signalProperties.add((IUnlistedProperty) f.get(null));
                    } catch (final IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        signalProperties.forEach(property -> {
            System.out.println(property.toString());
        });
    }
}
