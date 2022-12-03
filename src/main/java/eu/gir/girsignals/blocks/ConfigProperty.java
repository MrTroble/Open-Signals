package eu.gir.girsignals.blocks;

import java.util.function.Predicate;

import eu.gir.girsignals.SEProperty;

@SuppressWarnings("rawtypes")
public class ConfigProperty {

    public final Predicate predicate;
    public final SEProperty property;
    public final Object value;

    public ConfigProperty(final SEProperty property, final Object value) {
        this(t -> true, property, value);
    }

    public ConfigProperty(final Predicate predicate, final SEProperty property,
            final Object value) {
        super();
        this.predicate = predicate;
        this.property = property;
        this.value = value;
    }

}