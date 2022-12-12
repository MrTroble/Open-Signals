package com.troblecodings.signals.enums;

import java.util.function.BiConsumer;

import com.troblecodings.guilib.ecs.entitys.UIEntity;
import com.troblecodings.signals.signalbox.SignalBoxRenderUtil;

public enum EnumGuiMode {

    STRAIGHT(0, 0.5, 1, 0.5), CORNER(0, 0.5, 0.5, 1), END(1, 0.30, 1, 0.70),
    PLATFORM((parent, color) -> SignalBoxRenderUtil.drawRect(0, 0, ((UIEntity) parent).getWidth(),
            ((UIEntity) parent).getHeight() / 3, color)),
    BUE((parent, color) -> {
        final int part = ((UIEntity) parent).getHeight() / 3;
        SignalBoxRenderUtil.drawLines(0, ((UIEntity) parent).getWidth(), part, part, color);
        SignalBoxRenderUtil.drawLines(0, ((UIEntity) parent).getWidth(), part * 2, part * 2, color);
    }), HP(0), VP(1), RS(2), RA10(3);

    /**
     * Naming
     */

    public final BiConsumer<Object, Integer> consumer;

    private EnumGuiMode(final int id) {
        this.consumer = (parent, color) -> SignalBoxRenderUtil.drawTextured((UIEntity) parent, id);
    }

    private EnumGuiMode(final double x1, final double y1, final double x2, final double y2) {
        this.consumer = (parent, color) -> SignalBoxRenderUtil.drawLines(
                (int) (x1 * ((UIEntity) parent).getWidth()),
                (int) (x2 * ((UIEntity) parent).getWidth()),
                (int) (y1 * ((UIEntity) parent).getHeight()),
                (int) (y2 * ((UIEntity) parent).getHeight()), color);
    }

    private EnumGuiMode(final BiConsumer<Object, Integer> consumer) {
        this.consumer = consumer;
    }

}
