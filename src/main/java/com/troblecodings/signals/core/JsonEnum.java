package com.troblecodings.signals.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

import net.minecraftforge.client.model.data.ModelProperty;

public class JsonEnum extends ModelProperty<String> implements IIntegerable<String> {

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
        try {
            return this.valueToInt.get(value.toLowerCase());
        } catch (NullPointerException e) {
            throw new NullPointerException(value + " in " + name + " not valid!");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isValid(final String value) {
        return valueToInt.containsKey(value.toLowerCase());
    }

    public Class<String> getType() {
        return String.class;
    }

    public String valueToString(final String value) {
        return value;
    }

    public Collection<String> getAllowedValues() {
        if (values != null) {
            return values;
        }
        return new ArrayList<>();
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