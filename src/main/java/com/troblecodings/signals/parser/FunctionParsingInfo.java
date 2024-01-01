package com.troblecodings.signals.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.contentpacks.ContentPackException;

@SuppressWarnings("rawtypes")
public class FunctionParsingInfo {

    private static final HashMap<Class, Function<FunctionParsingInfo, Object>> PARAMETER_PARSER = new HashMap<>();

    private final HashMap<String, SEProperty> propertyCache = new HashMap<>();
    private final HashMap<String, ValuePack> predicateCache = new HashMap<>();
    private final HashMap<String, StringInteger> strIntCache = new HashMap<>();

    static {
        PARAMETER_PARSER.put(SEProperty.class, FunctionParsingInfo::getProperty);
        PARAMETER_PARSER.put(ValuePack.class, FunctionParsingInfo::getPredicate);
        PARAMETER_PARSER.put(StringInteger.class, FunctionParsingInfo::getStringInt);
        PARAMETER_PARSER.put(String.class, FunctionParsingInfo::getString);
        PARAMETER_PARSER.put(Boolean.class, FunctionParsingInfo::getBoolean);
    }

    public String argument;
    public final String signalName;
    public final List<SEProperty> properties;
    private Map<String, MethodInfo> translationTable;

    public FunctionParsingInfo(final Map<String, MethodInfo> translationTable,
            final Signal signal) {
        this(signal);
        this.translationTable = translationTable;
    }

    public FunctionParsingInfo(final Signal signalSystem) {
        this(Objects.requireNonNull(signalSystem).getSignalTypeName(),
                signalSystem.getProperties());
    }

    public FunctionParsingInfo(final String signalSystem, final List<SEProperty> properties) {
        this.argument = "";
        this.signalName = signalSystem;
        this.properties = properties;
        this.translationTable = LogicParser.TRANSLATION_TABLE;
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
        final SEProperty property = propertyCache.computeIfAbsent(name, _u -> {
            return properties.stream()
                    .filter(noneCache -> noneCache.getName().equalsIgnoreCase(name)).findAny()
                    .orElse(null);
        });
        if (property == null) {
            throw new LogicalParserException(
                    String.format("Could not make predicate=%s with system=%S!", argument,
                            signalName) + " Valid Properties: " + properties);
        }
        return property;
    }

    public Object getPredicate() {
        final ValuePack predicate = predicateCache.computeIfAbsent(argument.toLowerCase(), _u -> {
            final String[] parts = argument.split("\\.");
            if (parts.length != 2)
                throw new LogicalParserException(String.format(
                        "Syntax error predicate need to have the form PROPERTY.NAME but was %s",
                        argument));
            argument = parts[0];
            final SEProperty property = (SEProperty) getProperty();
            final String value = parts[1].toUpperCase();
            if (!property.getParent().isValid(new String(value)))
                throw new ContentPackException("[" + value + "] is not a valid state of " + property
                        + "! Valid States: " + property.getParent().getAllowedValues());
            return new ValuePack(property, ext -> ext.equals(value));
        });
        if (predicate == null)
            throw new LogicalParserException(String
                    .format("Could not make predicate=%s with system=%S!", argument, signalName));
        return predicate;
    }

    public Object getStringInt() {
        final StringInteger strInt = strIntCache.computeIfAbsent(argument.toLowerCase(), _u -> {
            final String[] parts = argument.split("\\.");
            if (parts.length != 2)
                throw new LogicalParserException(String.format(
                        "Syntax error speedValue need to have the form PROPERTY.NAME but was %s",
                        argument));
            return new StringInteger(parts[0], Integer.parseInt(parts[1]));
        });
        return strInt;
    }

    public Object getString() {
        return argument;
    }
    
    public Object getBoolean() {
        return Boolean.valueOf(argument);
    }

    public Map<String, MethodInfo> getTable() {
        return translationTable;
    }
}