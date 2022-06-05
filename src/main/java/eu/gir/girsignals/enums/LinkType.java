package eu.gir.girsignals.enums;

import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import net.minecraft.util.math.BlockPos;

public enum LinkType {
    SIGNAL(PathEntryType.SIGNAL), INPUT(PathEntryType.RESETING), OUTPUT(PathEntryType.OUTPUT);

    private final PathEntryType<BlockPos> type;

    private LinkType(final PathEntryType<BlockPos> type) {
        this.type = type;
    }

}
