package com.troblecodings.signals.signalbox;

import java.util.function.Consumer;

import com.troblecodings.signals.signalbox.config.ConfigInfo;

import net.minecraft.core.BlockPos;

public class WorldOperations {

    public void loadAndConfig(final int speed, final BlockPos currentPosition,
            final BlockPos nextPosition, final Consumer<ConfigInfo> infoChange) {
    }

    public void config(final ConfigInfo info) {
    }

    public void syncClient(final BlockPos pos) {
    }

    public void loadAndReset(final BlockPos position) {
    }

    public void setPower(final BlockPos position, final boolean power) {
    }

    public boolean isPowered(final BlockPos position) {
        return false;
    }

}
