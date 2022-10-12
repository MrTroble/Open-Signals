package eu.gir.girsignals.models.parser;

import java.util.function.Function;
import java.util.function.Predicate;

public class MethodInfo {

    public final String name;
    @SuppressWarnings("rawtypes")
    public final Class[] parameter;
    @SuppressWarnings("rawtypes")
    public final Function<Object[], Predicate> blockState;

    @SuppressWarnings("rawtypes")
    public MethodInfo(final String name, final Function<Object[], Predicate> blockState,
            final Class... parameter) {
        this.name = name;
        this.parameter = parameter;
        this.blockState = blockState;
    }

}
