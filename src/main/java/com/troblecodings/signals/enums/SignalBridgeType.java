package com.troblecodings.signals.enums;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.signalbridge.SignalBridgeBaseBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBlockProperties;
import com.troblecodings.signals.signalbridge.SignalBridgeCantileverBlock;

import net.minecraft.resources.ResourceLocation;

public enum SignalBridgeType {

    BASE, MAST, CANTILEVER;

    public SignalBridgeBasicBlock createNewBlock(final String name,
            final SignalBridgeBlockProperties properties) {
        final ResourceLocation location = new ResourceLocation(OpenSignalsMain.MODID,
                name.toLowerCase());
        SignalBridgeBasicBlock block;
        switch (this) {
            case BASE: {
                block = new SignalBridgeBaseBlock(properties);
                break;
            }
            case CANTILEVER: {
                block = new SignalBridgeCantileverBlock(properties);
                break;
            }
            case MAST: {
                block = new SignalBridgeCantileverBlock(properties);
                break;
            }
            default: {
                block = new SignalBridgeBasicBlock(properties);
                break;
            }
        }
        block.setRegistryName(location);
        if (OSBlocks.BLOCKS_TO_REGISTER.contains(block)) {
            throw new ContentPackException("Block for [" + name.toLowerCase() + "] alredy exists!");
        }
        OSBlocks.BLOCKS_TO_REGISTER.add(block);
        return block;
    }

}