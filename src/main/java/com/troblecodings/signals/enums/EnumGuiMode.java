package com.troblecodings.signals.enums;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.core.OSSupplier;
import com.troblecodings.signals.guis.UISignalBoxTile;

public enum EnumGuiMode {
    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }), PLATFORM((state) -> new UILines(new float[] {
            0, 0.15f, 1, 0.15f
    }, 3), PathwayModeType.NONE), BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), HP(0, true, PathwayModeType.START_END), VP(1, true, PathwayModeType.NONE),
    RS(2, true, PathwayModeType.START_END), RA10(3, PathwayModeType.END),
    SH2(4, PathwayModeType.NONE),
    IN_CONNECTION((_u) -> new UITexture(UISignalBoxTile.INCOMING_ICON), PathwayModeType.START),
    OUT_CONNECTION((_u) -> new UITexture(UISignalBoxTile.OUTGOING_ICON), PathwayModeType.END),
    ARROW((_u) -> new UITexture(UISignalBoxTile.ARROW_ICON), PathwayModeType.END),
    NE1((_u) -> new UITexture(UISignalBoxTile.NE1_ICON), PathwayModeType.START_END),
    NE5((_u) -> new UITexture(UISignalBoxTile.NE5_ICON), PathwayModeType.END),
    ZS3((_u) -> new UITexture(UISignalBoxTile.ZS3_ICON), PathwayModeType.NONE);

    /**
     * Naming
     */

    public final OSSupplier<Object> consumer;
    private final PathwayModeType type;

    private EnumGuiMode(final int id, final PathwayModeType type) {
        this((_u) -> new UITexture(UISignalBoxTile.ICON, id * 0.2, 0, id * 0.2 + 0.2, 0.5), type);
    }

    private EnumGuiMode(final int id, final boolean unused, final PathwayModeType type) {
        this((state) -> {
            switch (state) {
                case GREEN: {
                    return new UITexture(UISignalBoxTile.SIGNALS, id * 0.0667f, 0,
                            id * 0.066667f + 0.06f, 1);
                }
                case RED: {
                    return new UITexture(UISignalBoxTile.SIGNALS, id * 0.067f + 3 * 0.0666667f, 0,
                            id * 0.066667f + 3 * 0.067f + 0.06f, 1);
                }
                case OFF: {
                    return new UITexture(UISignalBoxTile.SIGNALS, id * 0.067f + 6 * 0.0666667f, 0,
                            id * 0.066667f + 6 * 0.067f + 0.06f, 1);
                }
                case SUBSIDIARY_GREEN: {
                    final int factor = 9;
                    return new UITexture(UISignalBoxTile.SIGNALS, (id + factor) * 0.0666667f, 0,
                            id * 0.066667f + factor * 0.067f + 0.06f, 1);
                }
                case SUBSIDIARY_RED: {
                    final int factor = 10;
                    return new UITexture(UISignalBoxTile.SIGNALS, (id + factor) * 0.0666667f, 0,
                            (id + factor) * 0.066667f + 0.06f, 1);
                }
                case SUBSIDIARY_OFF: {
                    final int factor = 11;
                    return new UITexture(UISignalBoxTile.SIGNALS, (id + factor) * 0.0666667f, 0,
                            (id + factor) * 0.066667f + 0.06f, 1);
                }
                default:
                    return new UITexture(UISignalBoxTile.SIGNALS);
            }
        }, type);
    }

    private EnumGuiMode(final float[] array) {
        this((_u) -> new UILines(array, 2), PathwayModeType.NONE);
    }

    private EnumGuiMode(final OSSupplier<Object> consumer, final PathwayModeType type) {
        this.consumer = consumer;
        this.type = type;
    }

    public PathwayModeType getModeType() {
        return type;
    }

    public static EnumGuiMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }
}