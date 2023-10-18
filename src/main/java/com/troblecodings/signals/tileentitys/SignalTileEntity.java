package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    private final Map<SEProperty, String> properties = new HashMap<>();

    private final SignalStateListener listener = (info, states, changed) -> {
        switch (changed) {
            case ADDED_TO_FILE:
            case ADDED_TO_CACHE: {
                properties.clear();
                properties.putAll(SignalStateHandler.getStates(info));
                break;
            }
            case REMOVED_FROM_FILE:
            case REMOVED_FROM_CACHE: {
                properties.clear();
                break;
            }
            case UPDATED: {
                properties.putAll(states);
                break;
            }
            default:
                break;
        }
    };

    @Override
    public boolean isValid(final EntityPlayer player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        getSignal().renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty() ? getSignal().getSignalTypeName() : name;
    }

    public Signal getSignal() {
        return (Signal) getBlockType();
    }

    public Map<SEProperty, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) {
            final IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
            world.markBlockRangeForRenderUpdate(pos, pos);
            markDirty();
            SignalStateHandler.addListener(new SignalStateInfo(world, pos, getSignal()), listener);
        }
    }

    @Override
    public void onChunkUnload() {
        if (!world.isRemote) {
            SignalStateHandler.removeListener(new SignalStateInfo(world, pos, getSignal()),
                    listener);
        }
    }
}