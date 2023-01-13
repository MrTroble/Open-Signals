package com.troblecodings.signals.core;

import java.nio.ByteBuffer;

import com.troblecodings.guilib.ecs.ContainerBase;

public class GuiObserver implements Observer {

    private final ContainerBase base;

    public GuiObserver(final ContainerBase container) {
        this.base = container;
    }

    @Override
    public void update(final ByteBuffer buffer) {

    }

}