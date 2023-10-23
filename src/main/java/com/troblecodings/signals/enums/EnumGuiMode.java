package com.troblecodings.signals.enums;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.core.OSSupplier;
import com.troblecodings.signals.guis.UISignalBoxTile;
import com.troblecodings.signals.signalbox.MainSignalIdentifier.SignalState;

public enum EnumGuiMode {
    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }), PLATFORM((state) -> new UILines(new float[] {
            0, 0.15f, 1, 0.15f
    }, 3)), BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), HP(0, true), VP(1, true), RS(2, true), RA10(3), SH2(4),
    IN_CONNECTION((_u) -> new UITexture(UISignalBoxTile.ARROW_ICON)),
    OUT_CONNECTION((_u) -> new UITexture(UISignalBoxTile.ARROW_ICON));

    /**
     * Naming
     */

    public final OSSupplier<Object> consumer;

    private EnumGuiMode(final int id) {
        this((_u) -> new UITexture(UISignalBoxTile.ICON, id * 0.2, 0, id * 0.2 + 0.2, 0.5));
    }

    private EnumGuiMode(final int id, final boolean unused) {
        this((state) -> {
            if (state.equals(SignalState.RED)) {
                return new UITexture(UISignalBoxTile.SIGNALS, id * 0.286f, 0, id * 0.286f + 0.143f, 1);
            } else {
                return new UITexture(UISignalBoxTile.SIGNALS, id * 0.286f + 0.143f, 0, id * 0.286f + 0.286f, 1);
            }
        });
    }

    private EnumGuiMode(final float[] array) {
        this((_u) -> new UILines(array, 2));
    }

    private EnumGuiMode(final OSSupplier<Object> consumer) {
        this.consumer = consumer;
    }

    public static EnumGuiMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }
}