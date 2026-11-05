package com.troblecodings.signals.enums;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBlockProperties;

public enum SignalBridgeType {

    BASE, MAST, MAST_HEAD, CANTILEVER, CANTILEVER_END;

    public SignalBridgeBasicBlock createNewBlock(final String name,
            final SignalBridgeBlockProperties properties) {
        final SignalBridgeBasicBlock block = new SignalBridgeBasicBlock(properties);
        block.setBlockName(name.toLowerCase());
        if (OSBlocks.BLOCKS_TO_REGISTER.contains(block)) {
            OpenSignalsMain.exitMinecraftWithMessage(
                    "Block for [" + name.toLowerCase() + "] alredy exists!");
        }
        OSBlocks.BLOCKS_TO_REGISTER.add(block);
        return block;
    }

}