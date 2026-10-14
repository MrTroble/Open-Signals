package com.troblecodings.signals.enums;

import java.util.function.Function;

import com.troblecodings.signals.guis.UISignalBoxProfile.OperationModeSettings;

public enum EnumPathUsage {

    FREE(mode -> mode.getFreeColor()), SELECTED(mode -> mode.getSelectColor()),
    BLOCKED(mode -> mode.getUsedColor()), PREPARED(mode -> mode.getPreparedColor()),
    PROTECTED(mode -> mode.getPreparedColor()), SHUNTING(mode -> mode.getShuntingColor());

    private final Function<OperationModeSettings, Integer> func;

    private EnumPathUsage(final Function<OperationModeSettings, Integer> func) {
        this.func = func;
    }

    /**
     * The color of this path status
     *
     * @return the color
     */
    public int getColor(final OperationModeSettings mode) {
        return func.apply(mode);
    }
}