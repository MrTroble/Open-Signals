package com.troblecodings.signals.models.parser;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.blocks.Signal;

import net.minecraftforge.common.property.IUnlistedProperty;

public class ParameterInfo {
    public String argument;
    public Signal system;
    @SuppressWarnings("rawtypes")
    public final List<IUnlistedProperty> properties;

    public ParameterInfo(final String argument, final Signal system) {
        this.argument = argument;
        this.system = system;
        this.properties = this.system != null ? system.getProperties() : ImmutableList.of();
    }

    public ParameterInfo(final String argument, final ParameterInfo info) {
        this.argument = argument;
        this.system = info.system;
        this.properties = info.properties;
    }
}