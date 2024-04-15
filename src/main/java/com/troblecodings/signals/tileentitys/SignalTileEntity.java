package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.IModelData;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    public SignalTileEntity(final TileEntityInfo info) {
        super(info);
    }

    private final Map<SEProperty, String> properties = new HashMap<>();

    private final SignalStateListener listener = (info, states, changed) -> {
        switch (changed) {
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
    public boolean isValid(final Player player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        final Signal signal = getSignal();
        if (signal == null)
            return;
        signal.renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        final Signal signal = getSignal();
        return name == null || name.isEmpty()
                ? signal != null ? signal.getSignalTypeName() : "signal"
                : name;
    }

    public Signal getSignal() {
        final Block block = getBlockState().getBlock();
        return block instanceof Signal ? (Signal) block : null;
    }

    public Map<SEProperty, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    @Override
    public @Nonnull IModelData getModelData() {
        final Map<SEProperty, String> states = ClientSignalStateHandler
                .getClientStates(new StateInfo(level, worldPosition));
        return new ModelInfoWrapper(states);
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide) {
            SignalStateHandler.addListener(new SignalStateInfo(level, worldPosition, getSignal()),
                    listener);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!level.isClientSide) {
            SignalStateHandler.removeListener(
                    new SignalStateInfo(level, worldPosition, getSignal()), listener);
        }
    }
}