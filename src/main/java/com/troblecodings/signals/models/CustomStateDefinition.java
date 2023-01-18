package com.troblecodings.signals.models;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class CustomStateDefinition<O, S extends StateHolder<O, S>> extends StateDefinition<O, S> {

    public CustomStateDefinition(final Function<O, S> p_61052_, final O p_61053_, final Factory<O, S> p_61054_,
            final Map<String, Property<?>> p_61055_) {
        super(p_61052_, p_61053_, p_61054_, p_61055_);
    }

    @Override
    public ImmutableList<S> getPossibleStates() {
        return ImmutableList.of();
    }

}
