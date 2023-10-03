package com.troblecodings.signals.enums;

import java.util.function.Supplier;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.entitys.render.UILines;
import com.troblecodings.guilib.ecs.entitys.render.UITexture;
import com.troblecodings.signals.guis.UISignalBoxTile;

public enum EnumGuiMode {
    STRAIGHT(new float[] {
            0, 0.5f, 1, 0.5f
    }), CORNER(new float[] {
            0, 0.5f, 0.5f, 1
    }), END(new float[] {
            0.9f, 0.2f, 0.9f, 0.8f
    }), PLATFORM(() -> new UILines(new float[] {
            0, 0.15f, 1, 0.15f
    }, 3)), BUE(new float[] {
            0.3f, 0, 0.3f, 1, 0.7f, 0, 0.7f, 1
    }), HP(0), VP(1), RS(2), RA10(3), SH2(4), IN_CONNECTION(() -> new UILines(new float[] {
            0, 0.5f, 1, 0.5f, 0.5f, 0.05f, 0.95f, 0.5f, 0.5f, 0.95f, 0.95f, 0.5f
    }, 2)), OUT_CONNECTION(new float[] {
            0, 0.5f, 1, 0.5f, 0.05f, 0.5f, 0.5f, 0.05f, 0.05f, 0.5f, 0.5f, 0.95f
    });

    /**
     * Naming
     */

    public final Supplier<Object> consumer;

    private EnumGuiMode(final int id) {
        this(() -> new UITexture(UISignalBoxTile.ICON, id * 0.2, 0, id * 0.2 + 0.2, 0.5));
    }

    private EnumGuiMode(final float[] array) {
        this(() -> new UILines(array, 2));
    }

    private EnumGuiMode(final Supplier<Object> consumer) {
        this.consumer = consumer;
    }

    public static EnumGuiMode of(final ReadBuffer buffer) {
        return values()[buffer.getByteToUnsignedInt()];
    }
}