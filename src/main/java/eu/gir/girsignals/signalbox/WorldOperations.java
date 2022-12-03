package eu.gir.girsignals.signalbox;

import java.util.function.Consumer;

import eu.gir.girsignals.signalbox.config.ConfigInfo;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import net.minecraft.util.math.BlockPos;

public class WorldOperations implements IChunkloadable {

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
