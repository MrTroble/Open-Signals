package eu.gir.girsignals.signalbox;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import eu.gir.girsignals.blocks.RedstoneIO;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldLoadOperations implements IChunkloadable {

    private final World world;

    public WorldLoadOperations(final @Nullable World world) {
        this.world = world;
    }

    public void loadAndConfig(final int speed, final BlockPos currentPosition,
            final BlockPos nextPosition, final Consumer<ConfigInfo> infoChange) {
        if (world == null)
            return;
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

    public void config(final ConfigInfo info) {
        if (world == null)
            return;
        final Signal current = info.current.getSignal();
        final ISignalAutoconfig config = current.getConfig();
        if (config == null)
            return;
        config.change(info);
    }

    public void syncClient(final BlockPos pos) {
        if (world == null)
            return;
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    public void loadAndReset(final BlockPos position) {
        if (world == null)
            return;
        loadChunkAndGetTile(SignalTileEnity.class, world, position, (signaltile, chunk) -> {
            final ISignalAutoconfig config = signaltile.getSignal().getConfig();
            if (config == null)
                return;
            config.reset(signaltile);
            syncClient(position);
        });
    }

    public void setPower(final BlockPos position, final boolean power) {
        if (position == null || world == null)
            return;
        loadChunkAndGetBlock(world, position, (state, chunk) -> {
            if (!(state.getBlock() instanceof RedstoneIO))
                return;
            final IBlockState ibstate = state.withProperty(RedstoneIO.POWER, power);
            world.setBlockState(position, ibstate, 3);
        });
    }

}
