package eu.gir.girsignals.enums;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.FREE_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.SELECTED_COLOR;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.USED_COLOR;

public enum EnumPathUsage {

    FREE(FREE_COLOR), SELECTED(SELECTED_COLOR), BLOCKED(USED_COLOR);

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
