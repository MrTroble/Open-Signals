package com.troblecodings.signals.signalbox;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.signalbox.config.ConfigInfo;
import com.troblecodings.signals.signalbox.config.ISignalAutoconfig;
import com.troblecodings.signals.signalbox.config.ISignalConfig;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldLoadOperations extends WorldOperations {

    private final World world;

    public WorldLoadOperations(final World world) {
        this.world = Objects.requireNonNull(world);
    }

    @Override
    public void loadAndConfig(final int speed, final BlockPos currentPosition,
            final BlockPos nextPosition, final Consumer<ConfigInfo> infoChange) {
        loadChunkAndGetTile(SignalTileEnity.class, world, currentPosition, (currentTile, chunk) -> {
            if (nextPosition == null) {
                final ConfigInfo info = new ConfigInfo(currentTile, null, speed);
                infoChange.accept(info);
                config(info);
            } else {
                loadChunkAndGetTile(SignalTileEnity.class, world, nextPosition, (nextTile, _u2) -> {
                    final ConfigInfo info = new ConfigInfo(currentTile, nextTile, speed);
                    infoChange.accept(info);
                    config(info);
                });
            }
            syncClient(currentPosition);
        });
    }

    @Override
    public void config(final ConfigInfo info) {
        if (world == null)
            return;
        final ISignalAutoconfig config = info.current.getSignal().getConfig();
        if (config == null) {
            ISignalConfig.change(info);
        } else {
            config.change(info);
        }

    }

    @Override
    public void syncClient(final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public void loadAndReset(final BlockPos position) {
        loadChunkAndGetTile(SignalTileEnity.class, world, position, (signaltile, chunk) -> {
            final ISignalAutoconfig config = signaltile.getSignal().getConfig();
            if (config == null) {
                ISignalConfig.reset(signaltile);
            } else {
                config.reset(signaltile);
            }

            syncClient(position);
        });
    }

    @Override
    public void setPower(final BlockPos position, final boolean power) {
        if (position == null)
            return;
        loadChunkAndGetBlock(world, position, (state, chunk) -> {
            if (!(state.getBlock() instanceof RedstoneIO))
                return;
            final IBlockState ibstate = state.withProperty(RedstoneIO.POWER, power);
            world.setBlockState(position, ibstate, 3);
        });
    }

    @Override
    public boolean isPowered(final BlockPos position) {
        if (position == null)
            return false;
        final AtomicBoolean atomic = new AtomicBoolean(false);
        loadChunkAndGetBlock(world, position, (state, chunk) -> {
            if (!(state.getBlock() instanceof RedstoneIO))
                return;
            if (!atomic.get())
                atomic.set(world.isBlockPowered(position));
        });
        return atomic.get();
    }

}
