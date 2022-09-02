package eu.gir.girsignals.models;

import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;

public class GIRBlockstateParser {

    public static Predicate<IExtendedBlockState> getBlockstatefromString(final String blockstate) {
        Predicate<IExtendedBlockState> state = null;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < blockstate.length(); i++) {
            final String str = builder.append(blockstate.charAt(i)).toString();
        }
        return state;
    }
}