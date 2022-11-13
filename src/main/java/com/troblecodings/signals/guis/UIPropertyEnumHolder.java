package com.troblecodings.signals.guis;

import com.troblecodings.guilib.ecs.entitys.UIEnumerable;
import com.troblecodings.signals.SEProperty;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@SuppressWarnings("rawtypes")
public class UIPropertyEnumHolder {

    private final SEProperty property;
    private final UIEnumerable enumarable;

    public UIPropertyEnumHolder(final SEProperty<?> property, final UIEnumerable enumarable) {
        this.property = property;
        this.enumarable = enumarable;
    }

    @SuppressWarnings("unchecked")
    public IExtendedBlockState apply(final IExtendedBlockState blockstate) {
        final Object value = property.getObjFromID(enumarable.getIndex());
        if (value == null)
            return blockstate;
        return blockstate.withProperty(property, value);
    }

}
