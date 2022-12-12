package com.troblecodings.signals.guis;

import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.signals.SEProperty;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("rawtypes")
public class UIPropertyEnumHolder {

    private final SEProperty property;
    private final UIEnumerable enumarable;

    public UIPropertyEnumHolder(final SEProperty property, final UIEnumerable enumarable) {
        this.property = property;
        this.enumarable = enumarable;
    }

    @SuppressWarnings("unchecked")
    public IModelData apply(final IModelData blockstate) {
        final Object value = property.getObjFromID(enumarable.getIndex());
        if (value == null)
            return blockstate;
        return blockstate.withProperty(property, value);
    }

}
