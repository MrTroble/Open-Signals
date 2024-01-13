package com.troblecodings.signals.guis;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;

public class SignalBridgeContainer extends ContainerBase {

    public SignalBridgeContainer(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        // TODO
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
    }

}
