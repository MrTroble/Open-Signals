package eu.gir.girsignals.models.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.utils.JsonEnum;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings("rawtypes")
public class FunctionParsingInfo {

    private static final HashMap<Class, Function<FunctionParsingInfo, Object>> PARAMETER_PARSER = new HashMap<>();

    private final HashMap<String, IUnlistedProperty> propertyCache = new HashMap<>();
    private final HashMap<String, ValuePack> predicateCache = new HashMap<>();

    static {
        PARAMETER_PARSER.put(IUnlistedProperty.class, FunctionParsingInfo::getProperty);
        PARAMETER_PARSER.put(ValuePack.class, FunctionParsingInfo::getPredicate);
        PARAMETER_PARSER.put(Integer.class, FunctionParsingInfo::getInt);
    }

    public String argument;
    public final String signalName;
    public final List<IUnlistedProperty> properties;

    public FunctionParsingInfo(final Signal signalSystem) {
        this(Objects.requireNonNull(signalSystem).getSignalTypeName(),
                signalSystem.getProperties());
    }

    public FunctionParsingInfo(final String signalSystem,
            final List<IUnlistedProperty> properties) {
        this.argument = "";
        this.signalName = signalSystem;
        this.properties = properties;
    }

    public Object[] getParameter(final Class[] parameter, final String[] arguments) {
        final Object[] parameters = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argument = arguments[i];
            parameters[i] = PARAMETER_PARSER.get(parameter[i]).apply(this);
        }
        return parameters;
    }

    public Object getProperty() {
        return this.getProperty(argument);
    }

    public Object getProperty(final String propertyName) {
        final String name = propertyName.toLowerCase();
        final IUnlistedProperty property = propertyCache.computeIfAbsent(name, _u -> {
            return properties.stream()
                    .filter(noneCache -> noneCache.getName().equalsIgnoreCase(name)).findAny()
                    .orElse(null);
        });
        if (property == null) {
            final IUnlistedProperty backup = JsonEnum.PROPERTIES.get(name);
            if (backup != null)
                return backup;
            throw new LogicalParserException(
                    String.format("Could not find property=%s in system=%S!", name, signalName));
        }
        return property;
    }

    @SuppressWarnings("unchecked")
    public Object getPredicate() {
        final ValuePack predicate = predicateCache.computeIfAbsent(argument.toLowerCase(), _u -> {
            final String[] parts = argument.split("\\.");
            if (parts.length != 2)
                throw new LogicalParserException(String.format(
                        "Syntax error predicate need to have the form PROPERTY.NAME but was %s",
                        argument));
            final String nextInfo = argument;
            argument = parts[0];
            final IUnlistedProperty property = (IUnlistedProperty) getProperty();
            final Class clazz = property.getType();
            try {
                final Method method = clazz.equals(String.class)
                        ? clazz.getMethod("valueOf", Object.class)
                        : clazz.getMethod("valueOf", String.class);
                final Object value = method.invoke(null, parts[1].toUpperCase());
                return new ValuePack(property, ext -> ext.equals(value));
            } catch (final IllegalArgumentException | NoSuchMethodException | SecurityException
                    | IllegalAccessException | InvocationTargetException e) {
                throw new LogicalParserException(
                        String.format("Property=%s is not a valid property [System: %s]", nextInfo,
                                signalName),
                        e);
            }
        });
        if (predicate == null)
            throw new LogicalParserException(String
                    .format("Could not make predicate=%s with system=%S!", argument, signalName));
        return predicate;
    }

    public Object getInt() {
        return Integer.parseInt(argument);
    }

}