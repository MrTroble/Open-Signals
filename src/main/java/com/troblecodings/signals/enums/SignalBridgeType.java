package com.troblecodings.signals.enums;

import com.troblecodings.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.init.OSBlocks;

import net.minecraft.resources.ResourceLocation;

public enum SignalBridgeType {

    BASE, MAST, MAST_HEAD, CANTILEVER, CANTILEVER_END;

    public SignalBridgeBasicBlock createNewBlock(final String name) {
        final SignalBridgeBasicBlock block = new SignalBridgeBasicBlock(this);
        block.setRegistryName(new ResourceLocation(OpenSignalsMain.MODID, name.toLowerCase()));
        if (OSBlocks.BLOCKS_TO_REGISTER.contains(block)) {
            throw new ContentPackException("Block for [" + name.toLowerCase() + "] alredy exists!");
        }
        OSBlocks.BLOCKS_TO_REGISTER.add(block);
        return block;
    }

}