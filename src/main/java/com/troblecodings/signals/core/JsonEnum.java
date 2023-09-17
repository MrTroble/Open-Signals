package com.troblecodings.signals.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

import net.minecraftforge.common.property.IUnlistedProperty;

public class JsonEnum implements IIntegerable<String>, IUnlistedProperty<String> {

    private final String name;
    private final List<String> values;
    private final Map<String, Integer> valueToInt;

    public JsonEnum(final String name, final List<String> values) {
        this.name = name;
        this.values = ImmutableList.copyOf(values);
        final Map<String, Integer> copyValues = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            copyValues.put(values.get(i).toLowerCase(), i);
        }
        valueToInt = ImmutableMap.copyOf(copyValues);
    }

    public int getIDFromValue(final String value) {
        final String name = value.toLowerCase();
        if (valueToInt.containsKey(name))
            return this.valueToInt.get(name);
        return -1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(final String value) {
        return valueToInt.containsKey(value.toLowerCase());
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String valueToString(final String value) {
        return value;
    }

    public Collection<String> getAllowedValues() {
        return values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, valueToInt, values);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final JsonEnum other = (JsonEnum) obj;
        return Objects.equals(name, other.name) && Objects.equals(valueToInt, other.valueToInt)
                && Objects.equals(values, other.values);
    }

    public Class<String> getValueClass() {
        return getType();
    }

    public Optional<String> parseValue(final String value) {
        if (isValid(value))
            return Optional.of(value);
        return Optional.absent();
    }

    public String getName(final String value) {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getObjFromID(final int obj) {
        if (obj < 0 || obj >= values.size())
            return "";
        return values.get(obj);
    }

    @Override
    public int count() {
        return values.size();
    }

    public static final JsonEnum BOOLEAN = new JsonEnum("boolean",
            ImmutableList.of("false", "true"));
}