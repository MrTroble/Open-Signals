package com.troblecodings.signals.enums;

import java.util.Arrays;

public enum PathType {

    NORMAL(EnumGuiMode.HP, EnumGuiMode.END, EnumGuiMode.IN_CONNECTION, EnumGuiMode.OUT_CONNECTION,
            EnumGuiMode.NE1, EnumGuiMode.NE5, EnumGuiMode.ARROW),
    SHUNTING(EnumGuiMode.RS, EnumGuiMode.RA10, EnumGuiMode.END, EnumGuiMode.ARROW), NONE();

    private final EnumGuiMode[] modes;

    private PathType(final EnumGuiMode... modes) {
        this.modes = modes;
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