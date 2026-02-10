package com.troblecodings.signals.handler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.enums.ChangedState;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientSignalStateHandler implements INetworkSync {

    private static final Map<StateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES =
            new HashMap<>();

    public static final Map<SEProperty, String> getClientStates(final StateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            return ImmutableMap
                    .copyOf(CURRENTLY_LOADED_STATES.computeIfAbsent(info, _u -> new HashMap<>()));
        }
    }

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            final World level = mc.world;
            final BlockPos signalPos = buffer.getBlockPos();
            final StateInfo stateInfo = new StateInfo(level, signalPos);
            final int signalID = buffer.getInt();
            final ChangedState changedState = buffer.getEnumValue(ChangedState.class);
            if (changedState.equals(ChangedState.REMOVED_FROM_CACHE)
                    || changedState.equals(ChangedState.REMOVED_FROM_FILE)) {
                setRemoved(stateInfo);
                return;
            }
            final Signal signal = Signal.getSignalByID(signalID);
            final Map<SEProperty, String> newProperties = buffer.getMapWithCombinedValueFunc(
                    NetworkBufferWrappers.getSEPropertyFunc(signal),
                    (buf, prop) -> prop.getObjFromID(buf.getByteToUnsignedInt()));
            final Map<SEProperty, String> properties;
            synchronized (CURRENTLY_LOADED_STATES) {
                properties =
                        CURRENTLY_LOADED_STATES.computeIfAbsent(stateInfo, _u -> new HashMap<>());
                properties.putAll(newProperties);
                CURRENTLY_LOADED_STATES.put(stateInfo, properties);
            }
            if (level == null)
                return;
            final Chunk chunk = level.getChunkFromBlockCoords(signalPos);
            if (chunk == null)
                return;
            final IBlockState state = level.getBlockState(signalPos);
            if (state == null)
                return;
            level.notifyBlockUpdate(signalPos, state, state, 3);
            mc.renderGlobal.notifyLightSet(signalPos);
            mc.renderGlobal.notifyBlockUpdate(level, signalPos, state, state, 8);
            final TileEntity tile = level.getTileEntity(signalPos);
            if (tile != null && tile instanceof SignalTileEntity) {
                ((SignalTileEntity) tile).updateAnimationState(properties, changedState);
            }
            final BlockState state = entity.getBlockState();
            mc.level.setBlocksDirty(signalPos, state, state);
            entity.requestModelDataUpdate();
            mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
        });
    }

    private static void setRemoved(final StateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            CURRENTLY_LOADED_STATES.remove(info);
        }
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}