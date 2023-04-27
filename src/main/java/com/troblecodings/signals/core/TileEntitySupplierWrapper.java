package com.troblecodings.signals.core;

import net.minecraft.tileentity.TileEntity;

public interface TileEntitySupplierWrapper {

    TileEntity supply(final TileEntityInfo info);
}