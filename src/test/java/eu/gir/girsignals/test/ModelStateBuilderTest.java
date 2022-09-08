package eu.gir.girsignals.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.GIRFileReader;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.models.GIRBlockstateParser;
import eu.gir.girsignals.models.ModelStats;
import eu.gir.girsignals.models.TextureStats;
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
        final Field[] fields = SignalHV.class.getDeclaredFields();
        for (final Field field : fields) {
            //System.out.println(field);
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

    public void testBlockstateParser() {
        final Object state = GIRBlockstateParser.getBlockstatefromString(
                "with(STOPSIGNAL, HP0) && (with(HPTYPE, STOPSIGNAL) || !has(HPTYPE))", null, null);
        System.out.println(state);
    }
}
