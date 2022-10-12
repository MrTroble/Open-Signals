package eu.gir.girsignals.models.parser;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings("rawtypes")
public class FunctionParsingInfo {

    private static final HashMap<Class, Function<FunctionParsingInfo, Object>> PARAMETER_PARSER = new HashMap<>();

    private final HashMap<String, IUnlistedProperty> propertyCache = new HashMap<>();
    private final HashMap<String, ValuePack> predicateCache = new HashMap<>();

    static {
        PARAMETER_PARSER.put(IUnlistedProperty.class, FunctionParsingInfo::getProperty);
        PARAMETER_PARSER.put(ValuePack.class, FunctionParsingInfo::getPredicate);
    }

    public final ParameterInfo info;

    public FunctionParsingInfo(final Signal signalSystem) {
        info = new ParameterInfo("", signalSystem);
    }

    public Object[] getParameter(final Class[] parameter, final String[] arguments) {
        final Object[] parameters = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            info.argument = arguments[i];
            parameters[i] = PARAMETER_PARSER.get(parameter[i]).apply(this);
        }
        return parameters;
    }

    public Object getProperty() {
        final IUnlistedProperty property = propertyCache
                .computeIfAbsent(info.argument.toLowerCase(), _u -> {
                    final List<IUnlistedProperty> properties = info.system.getProperties();
                    return properties.stream().filter(
                            noneCache -> noneCache.getName().equalsIgnoreCase(info.argument))
                            .findAny().orElse(null);
                });
        if (property == null)
            throw new LogicalParserException(
                    String.format("Could not find property=%s in system=%S!", info.argument,
                            info.system.getSignalTypeName()));
        return property;
    }

    @SuppressWarnings("unchecked")
    public Object getPredicate() {
        final ValuePack predicate = predicateCache.computeIfAbsent(info.argument.toLowerCase(),
                _u -> {
                    final String[] parts = info.argument.split("\\.");
                    if (parts.length != 2)
                        throw new LogicalParserException(String.format(
                                "Syntax error predicate need to have the form PROPERTY.NAME but was %s",
                                info.argument));
                    final String nextInfo = info.argument;
                    info.argument = parts[0];
                    final IUnlistedProperty property = (IUnlistedProperty) getProperty();
                    final Class clazz = property.getType();
                    if (!clazz.isEnum())
                        throw new LogicalParserException(
                                String.format("Property=%s is not a enum property but must be",
                                        nextInfo, info.system.getSignalTypeName()));
                    try {
                        final Object value = Enum.valueOf(clazz, parts[1].toUpperCase());
                        return new ValuePack(property, ext -> ext.equals(value));
                    } catch (final IllegalArgumentException e) {
                        throw new LogicalParserException(e);
                    }
                });
        if (predicate == null)
            throw new LogicalParserException(
                    String.format("Could not make predicate=%s with system=%S!", info.argument,
                            info.system.getSignalTypeName()));
        return predicate;
    }

}
