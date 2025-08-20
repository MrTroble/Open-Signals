package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.animation.SignalAnimationHandler;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.SignalStateListener;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.ClientSignalStateHandler;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

public class SignalTileEntity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    protected final SignalAnimationHandler handler;

    public SignalTileEntity(final TileEntityInfo info) {
        super(info);
        this.handler = new SignalAnimationHandler(this);

    }

    private final Map<SEProperty, String> properties = new HashMap<>();

    private final SignalStateListener listener = (info, states, changed) -> {
        synchronized (properties) {
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
        }
    };

    @Override
    public boolean isValid(final PlayerEntity player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        getSignal().renderOverlay(info.with(this));
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

    public SignalAnimationHandler getAnimationHandler() {
        return handler;
    }

    @Override
    public void requestModelDataUpdate() {
        final Map<SEProperty, String> newProperties = ClientSignalStateHandler
                .getClientStates(new StateInfo(level, worldPosition));
        synchronized (properties) {
            this.properties.clear();
            this.properties.putAll(newProperties);
        }
        handler.updateStates(newProperties, properties);
        super.requestModelDataUpdate();
    }

    @Override
    public @Nonnull IModelData getModelData() {
        return new ModelInfoWrapper(properties);
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide) {
            SignalStateHandler.addListener(new SignalStateInfo(level, worldPosition, getSignal()),
                    listener);
        } else {
            if (getSignal().hasAnimation()) {
                handler.updateAnimationListFromBlock();
                final Map<SEProperty, String> newProperties = ClientSignalStateHandler
                        .getClientStates(new StateInfo(level, worldPosition));
                handler.updateStates(newProperties, properties);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (!level.isClientSide) {
            SignalStateHandler.removeListener(
                    new SignalStateInfo(level, worldPosition, getSignal()), listener);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getSignal().getRenderBox().map(box -> box.move(worldPosition))
                .orElse(super.getRenderBoundingBox());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public double getViewDistance() {
        if (getSignal().hasAnimation())
            return ConfigHandler.CLIENT.renderDistance.get();
        return super.getViewDistance();
    }
}