package eu.gir.girsignals.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.IUnlistedProperty;

public class JsonEnum implements IUnlistedProperty<String>, IProperty<String> {

    private final String name;
    private final List<String> values;
    private final Set<String> valueSet;

    public JsonEnum(final String name, final List<String> values) {
        this.name = name;
        this.values = ImmutableList.copyOf(values);
        this.valueSet = ImmutableSet.copyOf(values);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(final String value) {
        return valueSet.contains(value);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String valueToString(final String value) {
        return value;
    }

    @Override
    public Collection<String> getAllowedValues() {
        if (values != null) {
            return values;
        }
        return new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, valueSet, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final JsonEnum other = (JsonEnum) obj;
        return Objects.equals(name, other.name) && Objects.equals(valueSet, other.valueSet)
                && Objects.equals(values, other.values);
    }

    @Override
    public Class<String> getValueClass() {
        return getType();
    }

    @Override
    public Optional<String> parseValue(final String value) {
        if (isValid(value))
            return Optional.of(value);
        return Optional.absent();
    }

    @Override
    public String getName(final String value) {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }

    private static final Gson GSON = new Gson();
    public static final Map<String, JsonEnum> PROPERTIES = getProperties();

    @SuppressWarnings("unchecked")
    public static Map<String, JsonEnum> getProperties() {
        final HashMap<String, JsonEnum> returnmap = new HashMap<>();
        final Map<String, String> files = FileReader
                .readallFilesfromDierectory("/assets/girsignals/enumdefinition");
        files.forEach((_u, file) -> {
            final Map<String, List<String>> map = GSON.fromJson(file,
                    (Class<Map<String, List<String>>>) (Class<?>) Map.class);
            if (map == null)
                throw new IllegalStateException("Could not parse " + file);
            map.forEach(
                    (name, list) -> returnmap.put(name.toLowerCase(), new JsonEnum(name, list)));
        });
        return returnmap;
    }
}
