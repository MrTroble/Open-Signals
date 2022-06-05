package eu.gir.girsignals.enums;

import java.util.Arrays;

import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import net.minecraft.util.math.BlockPos;

public enum PathType {

    NORMAL(PathEntryType.SIGNAL, EnumGuiMode.HP),
    SHUNTING(PathEntryType.SIGNAL, EnumGuiMode.RS, EnumGuiMode.RA10), NONE(PathEntryType.SIGNAL);

    private final EnumGuiMode[] modes;
    private final PathEntryType<BlockPos> type;

    private PathType(final PathEntryType<BlockPos> type, final EnumGuiMode... modes) {
        this.modes = modes;
        this.type = type;
    }

    public boolean hasMode(final EnumGuiMode mode) {
        return Arrays.stream(modes).anyMatch(mode::equals);
    }

    /**
     * @return the modes
     */
    public EnumGuiMode[] getModes() {
        return modes;
    }

    public static final PathType of(final EnumGuiMode mode) {
        return Arrays.stream(PathType.values()).filter(type -> type.hasMode(mode)).findFirst()
                .orElse(NONE);
    }

}
