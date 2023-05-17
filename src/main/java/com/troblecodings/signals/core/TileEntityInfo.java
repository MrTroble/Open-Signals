package com.troblecodings.signals.core;

import net.minecraft.tileentity.TileEntityType;

public class TileEntityInfo {

    public TileEntityType<?> type;

    public TileEntityInfo with(final TileEntityType<?> type) {
        this.type = type;
        return this;
    }
}