package com.troblecodings.signals.enums;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.core.OSSupplier;
import com.troblecodings.signals.guis.GuiSignalBox;
import com.troblecodings.signals.guis.UISignalBoxTile;
import com.troblecodings.signals.signalbox.SignalBoxUtil;

public enum EnumGuiMode {
    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }, PathwayModeType.END, 0), PLATFORM((state) -> new UILines(new float[] {
            0, 0.15f, 1, 0.15f
    }, 3).setColor(SignalBoxUtil.FREE_COLOR), PathwayModeType.NONE, 0), BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), HP(0, true, PathwayModeType.START_END, 2), VP(1, true, PathwayModeType.NONE, 1),
    RS(2, true, PathwayModeType.START_END, 1), RA10(3, PathwayModeType.END, 1),
    SH2(4, PathwayModeType.NONE, 1),
    IN_CONNECTION((_u) -> new UITexture(UISignalBoxTile.INCOMING_ICON), PathwayModeType.START, 1),
    OUT_CONNECTION((_u) -> new UITexture(UISignalBoxTile.OUTGOING_ICON), PathwayModeType.END, 1),
    ARROW((_u) -> new UITexture(UISignalBoxTile.ARROW_ICON), PathwayModeType.END, 1),
    NE1((_u) -> new UITexture(UISignalBoxTile.NE1_ICON), PathwayModeType.START_END, 1),
    NE5((_u) -> new UITexture(UISignalBoxTile.NE5_ICON), PathwayModeType.START_END, 1),
    ZS3((_u) -> new UITexture(UISignalBoxTile.ZS3_ICON), PathwayModeType.NONE, 1),
    TRAIN_NUMBER((_u) -> new UILines(new float[] {
            0, 0.5f, 2, 0.5f
    }, 6).setColor(GuiSignalBox.TRAIN_NUMBER_BACKGROUND_COLOR), PathwayModeType.NONE, 2);

    /**
     * Naming
     */

    public final OSSupplier<Object> consumer;
    public final int translation;
    private final PathwayModeType type;

    private EnumGuiMode(final int id, final PathwayModeType type, final int translation) {
        this((_u) -> new UITexture(UISignalBoxTile.ICON, id * 0.2, 0, id * 0.2 + 0.2, 0.5), type,
                translation);
    }

    private EnumGuiMode(final int id, final boolean unused, final PathwayModeType type,
            final int translation) {
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
        }, type, translation);
    }

    private EnumGuiMode(final float[] array) {
        this(array, PathwayModeType.NONE, 0);
    }

    private EnumGuiMode(final float[] array, final PathwayModeType type, final int translation) {
        this((_u) -> new UILines(array, 2).setColor(SignalBoxUtil.FREE_COLOR), type, translation);
    }

    private EnumGuiMode(final OSSupplier<Object> consumer, final PathwayModeType type,
            final int translation) {
        this.consumer = consumer;
        this.type = type;
        this.translation = translation;
    }

    public PathwayModeType getModeType() {
        return type;
    }

    public static EnumGuiMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }
}