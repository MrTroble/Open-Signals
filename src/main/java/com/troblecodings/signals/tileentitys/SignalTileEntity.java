package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.model.data.ModelData;

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
        getSignal().renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty() ? getSignal().getSignalTypeName() : name;
    }

    public Signal getSignal() {
        return ((Signal) getBlockState().getBlock());
    }

    public Map<SEProperty, String> getProperties() {
        return ImmutableMap.copyOf(properties);
    }

    @Override
    public @NotNull ModelData getModelData() {
        return new ModelInfoWrapper(properties).getModelData();
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