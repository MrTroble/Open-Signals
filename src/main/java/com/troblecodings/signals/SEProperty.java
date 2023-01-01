package com.troblecodings.signals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.utils.JsonEnum;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.world.ChunkEvent;

public class SEProperty extends ModelProperty<String>
        implements IIntegerable<String> {

    private final String name;
    private final JsonEnum parent;
    private final String defaultValue;
    private final ChangeableStage stage;
    private final Predicate<Map<SEProperty, String>> deps;
    private final List<String> allowedValues;

    public SEProperty(final String name, final JsonEnum parent, final String defaultValue,
            final ChangeableStage stage, final Predicate<Map<SEProperty, String>> deps) {
        this.name = name;
        this.parent = parent;
        this.defaultValue = defaultValue;
        this.stage = stage;
        this.deps = deps;
        if (parent.getAllowedValues() == null) {
            this.allowedValues = new ArrayList<>();
        } else {
            this.allowedValues = ImmutableList.copyOf(parent.getAllowedValues());
        }
    }

    public Object getWrapper(Object object) {
    	return ((IModelData)object).getData(this);
    }
    
    @Override
    public String getName() {
        return name;
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

    public NBTWrapper writeToNBT(final NBTWrapper comp, final Object value) {
        if (value != null && isValid((String) value))
            comp.putInteger(getName(), this.allowedValues.indexOf(value));
        return comp;
    }

    public boolean isValid(final int id) {
        return id > -1 && id < this.count();
    }

    @Override
    public String getObjFromID(final int pObjID) {
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

    public static SEProperty cst(final Object iup) {
        return (SEProperty) iup;
    }

    public JsonEnum getParent() {
        return parent;
    }

    public static class SEAutoNameProp extends SEProperty {

        public SEAutoNameProp(final String name, final JsonEnum parent, final String defaultValue,
                final ChangeableStage stage, final Predicate<Map<SEProperty, String>> deps) {
            super(name, parent, defaultValue, stage, deps);
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public String getNamedObj(final int obj) {
            return I18n.get("property." + this.getName() + ".name") + ": " + getObjFromID(obj);
        }
    }

}
