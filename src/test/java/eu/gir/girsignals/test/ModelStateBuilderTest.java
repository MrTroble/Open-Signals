package eu.gir.girsignals.test;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.models.Texture;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelStateBuilderTest {

    private final Texture texture = new Texture();

    @Test
    public void testModelstats() {
        final Predicate<IExtendedBlockState> blockstate = texture.getPredicates();
        System.out.println(blockstate);
    }
}
