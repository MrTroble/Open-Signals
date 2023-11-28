package com.troblecodings.signals.enums;

import static com.troblecodings.signals.signalbox.SignalBoxUtil.FREE_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.USED_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.PREPARED_COLOR;

public enum EnumPathUsage {

    FREE(FREE_COLOR), SELECTED(SELECTED_COLOR), BLOCKED(USED_COLOR), PREPARED(PREPARED_COLOR);

    private final int color;

    private EnumPathUsage(final int color) {
        this.color = color;
    }

    /**
     * The color of this path status
     *
     * @return the color
     */
    public int getColor() {
        return color;
    }
}