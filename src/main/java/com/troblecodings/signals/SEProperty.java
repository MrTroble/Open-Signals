package com.troblecodings.signals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SEProperty<T extends Comparable<T>>
        implements IUnlistedProperty<T>, IIntegerable<T>, Predicate<Map<SEProperty<?>, Object>> {

    private final String name;
    private final IProperty<T> parent;
    private final T defaultValue;
    private final ChangeableStage stage;
    private final Predicate<Map<SEProperty<?>, Object>> deps;
    private final List<T> allowedValues;

    public SEProperty(final String name, final IProperty<T> parent, final T defaultValue,
            final ChangeableStage stage, final Predicate<Map<SEProperty<?>, Object>> deps) {
        this.name = name;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.stage = stage;
        this.deps = deps;
        this.allowedValues = ImmutableList.copyOf(parent.getAllowedValues());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(final T value) {
        return this.allowedValues.contains(value);
    }

    @Override
    public Class<T> getType() {
        return parent.getValueClass();
    }

    @Override
    public String valueToString(final T value) {
        return parent.getName(value);
    }

    public Optional<T> readFromNBT(final NBTTagCompound comp) {
        if (comp.hasKey(this.getName())) {
            final int id = comp.getInteger(this.getName());
            return Optional.of(getObjFromID(id));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public NBTTagCompound writeToNBT(final NBTTagCompound comp, final Object value) {
        if (value != null && isValid((T) value))
            comp.setInteger(getName(), this.allowedValues.indexOf(value));
        return comp;
    }

    public boolean isValid(final int id) {
        return id > -1 && id < this.count();
    }

    @Override
    public T getObjFromID(final int pObjID) {
        return this.allowedValues.get(pObjID);
    }

    @Override
    public int count() {
        return this.allowedValues.size();
    }

    @SuppressWarnings("unchecked")
    public T cast(final Object value) {
        return (T) value;
    }

    public T getDefault() {
        return defaultValue;
    }

    public boolean isChangabelAtStage(final ChangeableStage stage) {
        return this.stage.equals(stage);
    }

    @Override
    public String toString() {
        return "SEP[" + this.getName() + "]";
    }

    @Override
    public boolean test(final Map<SEProperty<?>, Object> t) {
        return this.deps.test(t);
    }

    @SuppressWarnings("rawtypes")
    public static SEProperty<?> cst(final Object iup) {
        return (SEProperty) iup;
    }

    public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(final String name,
            final T defaultValue) {
        return of(name, defaultValue, ChangeableStage.APISTAGE);
    }

    public static SEProperty<Boolean> of(final String name, final boolean defaultValue) {
        return of(name, defaultValue, ChangeableStage.APISTAGE);
    }

    public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(final String name,
            final T defaultValue, final ChangeableStage stage) {
        return of(name, defaultValue, stage, true);
    }

    public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(final String name,
            final T defaultValue, final ChangeableStage stage, final boolean autoname) {
        return of(name, defaultValue, stage, autoname, t -> true);
    }

    public static SEProperty<Boolean> of(final String name, final boolean defaultValue,
            final ChangeableStage stage) {
        return of(name, defaultValue, stage, false);
    }

    public static SEProperty<Boolean> of(final String name, final boolean defaultValue,
            final ChangeableStage stage, final boolean autoname) {
        return of(name, defaultValue, stage, autoname, t -> true);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(final String name,
            final T defaultValue, final ChangeableStage stage, final boolean autoname,
            final Predicate<Map<SEProperty<?>, Object>> deps) {
        if (autoname)
            return new SEAutoNameProp<T>(name,
                    PropertyEnum.create(name, (Class<T>) defaultValue.getClass()), defaultValue,
                    stage, deps);
        return new SEProperty<T>(name,
                PropertyEnum.create(name, (Class<T>) defaultValue.getClass()), defaultValue, stage,
                deps);
    }

    public static SEProperty<Boolean> of(final String name, final boolean defaultValue,
            final ChangeableStage stage, final boolean autoname,
            final Predicate<Map<SEProperty<?>, Object>> deps) {
        if (autoname)
            return new SEAutoNameProp<Boolean>(name, PropertyBool.create(name), defaultValue, stage,
                    deps);
        return new SEProperty<Boolean>(name, PropertyBool.create(name), defaultValue, stage, deps);
    }

    public static class SEAutoNameProp<T extends Comparable<T>> extends SEProperty<T> {

        public SEAutoNameProp(final String name, final IProperty<T> parent, final T defaultValue,
                final ChangeableStage stage, final Predicate<Map<SEProperty<?>, Object>> deps) {
            super(name, parent, defaultValue, stage, deps);
        }

        @SideOnly(Side.CLIENT)
        @Override
        public String getNamedObj(final int obj) {
            return I18n.format("property." + this.getName() + ".name") + ": " + getObjFromID(obj);
        }
    }

}
