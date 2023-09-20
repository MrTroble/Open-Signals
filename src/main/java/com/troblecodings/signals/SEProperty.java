package com.troblecodings.signals;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.core.JsonEnum;
import com.troblecodings.signals.enums.ChangeableStage;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class SEProperty extends ModelProperty<String> implements IIntegerable<String> {

    private final String name;
    private final JsonEnum parent;
    private final String defaultValue;
    private final ChangeableStage stage;
    private final Predicate<Map<SEProperty, String>> deps;
    private final List<String> allowedValues;
    private final int itemDamage;

    public SEProperty(final String name, final JsonEnum parent, final String defaultValue,
            final ChangeableStage stage, final Predicate<Map<SEProperty, String>> deps,
            final int itemDamage) {
        this.name = name;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.stage = stage;
        this.deps = deps;
        this.allowedValues = ImmutableList.copyOf(parent.getAllowedValues());
        this.itemDamage = itemDamage;
    }

    public Object getWrapper(final Object object) {
        return ((IModelData) object).getData(this);
    }

    @Override
    public String getName() {
        return name;
    }

    public int getItemDamage() {
        return itemDamage;
    }

    public boolean isValid(final String value) {
        return this.allowedValues.contains(value);
    }

    public String valueToString(final String value) {
        return parent.getName(value);
    }

    public Optional<String> readFromNBT(final NBTWrapper comp) {
        if (comp.contains(this.getName())) {
            final int id = comp.getInteger(this.getName());
            return Optional.of(getObjFromID(id));
        }
        return Optional.empty();
    }

    public NBTWrapper writeToNBT(final NBTWrapper comp, final String value) {
        if (value != null && isValid(value))
            comp.putInteger(getName(), parent.getIDFromValue(value));
        return comp;
    }

    public boolean isValid(final int id) {
        return id > -1 && id < this.count();
    }

    @Override
    public String getObjFromID(final int pObjID) {
        if (pObjID < 0 || pObjID >= this.allowedValues.size())
            return "";
        return this.allowedValues.get(pObjID);
    }

    @Override
    public int count() {
        return this.allowedValues.size();
    }

    public String getDefault() {
        return defaultValue;
    }

    public boolean isChangabelAtStage(final ChangeableStage stage) {
        return this.stage.equals(stage);
    }

    @Override
    public String toString() {
        return "SEP[" + this.getName() + "]";
    }

    public boolean testMap(final Map<SEProperty, String> t) {
        return this.deps.test(t);
    }

    public JsonEnum getParent() {
        return parent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedValues, defaultValue, deps, name, parent, stage);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SEProperty other = (SEProperty) obj;
        return Objects.equals(allowedValues, other.allowedValues)
                && Objects.equals(defaultValue, other.defaultValue)
                && Objects.equals(deps, other.deps) && Objects.equals(name, other.name)
                && Objects.equals(parent, other.parent) && stage == other.stage;
    }

    public static class SEAutoNameProp extends SEProperty {

        public SEAutoNameProp(final String name, final JsonEnum parent, final String defaultValue,
                final ChangeableStage stage, final Predicate<Map<SEProperty, String>> deps,
                final int itemDamage) {
            super(name, parent, defaultValue, stage, deps, itemDamage);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public String getNamedObj(final int obj) {
            return I18n.get("property." + this.getName() + ".name") + ": " + getObjFromID(obj);
        }
    }

}
