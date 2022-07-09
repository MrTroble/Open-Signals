package eu.gir.girsignals.signalbox;

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

    public void loadAndConfig(final int speed, final BlockPos lastPosition,
            final BlockPos nextPosition) {
        loadAndConfig(speed, lastPosition, nextPosition, null);
    }

    public void loadAndConfig(final int speed, final BlockPos lastPosition,
            final BlockPos nextPosition, final ISignalAutoconfig override) {
        if (world == null)
            return;
        loadChunkAndGetTile(SignalTileEnity.class, world, lastPosition, (lastTile, chunk) -> {
            if (nextPosition == null) {
                config(speed, lastTile, null, override);
            } else {
                loadChunkAndGetTile(SignalTileEnity.class, world, nextPosition,
                        (nextTile, _u2) -> config(speed, lastTile, nextTile, override));
            }
            syncClient(lastPosition);
        });
    }

    public void config(final int speed, final SignalTileEnity lastTile,
            final SignalTileEnity nextTile, final ISignalAutoconfig override) {
        if (world == null)
            return;
        final Signal last = lastTile.getSignal();
        final ISignalAutoconfig config = override == null ? last.getConfig() : override;
        if (config == null)
            return;
        final ConfigInfo info = new ConfigInfo(lastTile, nextTile, speed);
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
