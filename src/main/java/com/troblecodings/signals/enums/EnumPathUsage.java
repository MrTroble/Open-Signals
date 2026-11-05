package com.troblecodings.signals.enums;

import static com.troblecodings.signals.signalbox.SignalBoxUtil.FREE_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.PREPARED_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.SHUNTING_COLOR;
import static com.troblecodings.signals.signalbox.SignalBoxUtil.USED_COLOR;

import java.util.function.IntSupplier;

public enum EnumPathUsage {

    FREE(FREE_COLOR), SELECTED(SELECTED_COLOR), BLOCKED(USED_COLOR), PREPARED(PREPARED_COLOR),
    PROTECTED(PREPARED_COLOR), SHUNTING(SHUNTING_COLOR);

    /**
     * Held as a supplier rather than an int: these come from the Forge config, which cannot be
     * read while this enum is being initialised.
     */
    private final IntSupplier color;

    private EnumPathUsage(final IntSupplier color) {
        this.color = color;
    }

    /**
     * The color of this path status
     *
     * @return the color
     */
    public int getColor() {
        return color.getAsInt();
    }
}
