package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.signals.OpenSignalsMain;

public class GuiObserver implements Observer {

    private final ContainerBase base;

    public GuiObserver(final ContainerBase container) {
        this.base = container;
    }

    @Override
    public void update(final ByteBuffer buffer) {
        OpenSignalsMain.network.sendTo(base.getInfo().player, buffer);
    }
}