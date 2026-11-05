package com.troblecodings.signals.handler;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

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

    private static final Set<BlockPos> PENDING_REBUILDS =
            Collections.synchronizedSet(new HashSet<>());

    public static void scheduleRebuild(final BlockPos pos) {
        PENDING_REBUILDS.add(pos);
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        final Minecraft mc = Minecraft.getInstance();
        mc.doRunTask(() -> {
            final ClientLevel level = mc.level;
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
            synchronized (CURRENTLY_LOADED_STATES) {
                final Map<SEProperty, String> existing = CURRENTLY_LOADED_STATES.remove(stateInfo);
                final Map<SEProperty, String> properties =
                        existing != null ? existing : new HashMap<>();
                properties.putAll(newProperties);
                CURRENTLY_LOADED_STATES.put(stateInfo, properties);
            }
            if (level == null)
                return;
            final long startTime = Calendar.getInstance().getTimeInMillis();
            SERVICE.execute(() -> {
                BlockEntity entity;
                while ((entity = level.getBlockEntity(signalPos)) == null) {
                    final long currentTime = Calendar.getInstance().getTimeInMillis();
                    if (currentTime - startTime >= 5000)
                        return;
                    continue;
                }
                if (!(entity instanceof SignalTileEntity))
                    return;
                final BlockState state = entity.getBlockState();
                mc.level.setBlocksDirty(signalPos, state, state);
                entity.requestModelDataUpdate();
                mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
            });
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

    public static Map<SEProperty, String> refreshAndGetClientStates(final StateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            Map<SEProperty, String> existing = CURRENTLY_LOADED_STATES.remove(info);
            if (existing == null) {
                existing = new HashMap<>();
            }
            CURRENTLY_LOADED_STATES.put(info, existing);
            return ImmutableMap.copyOf(existing);
        }
    }

}