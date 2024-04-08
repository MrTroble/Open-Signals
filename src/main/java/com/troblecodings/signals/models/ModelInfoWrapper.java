package com.troblecodings.signals.models;

import java.util.Map;
import java.util.Optional;

import com.troblecodings.core.interfaces.BlockModelDataWrapper;
import com.troblecodings.signals.SEProperty;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class ModelInfoWrapper implements BlockModelDataWrapper {

    public IBlockState state;

    public ModelInfoWrapper(final IBlockState state) {
        this.state = state;
    }

    public ModelInfoWrapper(final Block block) {
        this.state = block.getDefaultState();
    }

    public ModelInfoWrapper(final Block block, final Map<SEProperty, String> properties) {
        this(block);
        properties.forEach((property,
                value) -> state = ((IExtendedBlockState) state).withProperty(property, value));
    }

    @Override
    public IBlockState getBlockState() {
        return state;
    }

    public <T> boolean hasProperty(final IUnlistedProperty<T> prop) {
        return ((IExtendedBlockState) state).getUnlistedNames().contains(prop);
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(final IUnlistedProperty<T> prop) {
        final Optional<T> opt = (Optional<T>) ((IExtendedBlockState) state).getUnlistedProperties()
                .get(prop);
        if (opt.isPresent())
            return opt.get();
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T setData(final IUnlistedProperty<T> prop, final T value) {
        state = ((IExtendedBlockState) state).withProperty(prop, value);
        return (T) prop;
    }

    public boolean has(final SEProperty property) {
        return hasProperty(property);
    }

    public String get(final SEProperty property) {
        return getData(property);
    }

    public void set(final SEProperty property, final String value) {
        setData(property, value);
    }
}