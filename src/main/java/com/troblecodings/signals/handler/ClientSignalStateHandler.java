package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.ReadBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientSignalStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();

    public static final Map<SEProperty, String> getClientStates(final ClientSignalStateInfo info) {
        synchronized (CURRENTLY_LOADED_STATES) {
            return CURRENTLY_LOADED_STATES.getOrDefault(info, new HashMap<>());
        }
    }

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        final Minecraft mc = Minecraft.getInstance();
        final ClientLevel level = mc.level;
        final BlockPos signalPos = buffer.getBlockPos();
        final int signalID = buffer.getInt();
        final int propertiesSize = buffer.getByteAsInt();
        if (propertiesSize == 255) {
            setRemoved(signalPos);
            return;
        }
        final int[] propertyIDs = new int[propertiesSize];
        final int[] valueIDs = new int[propertiesSize];
        for (int i = 0; i < propertiesSize; i++) {
            propertyIDs[i] = buffer.getByteAsInt();
            valueIDs[i] = buffer.getByteAsInt();
        }
        final List<SEProperty> signalProperties = Signal.SIGNAL_IDS.get(signalID).getProperties();
        final ClientSignalStateInfo stateInfo = new ClientSignalStateInfo(level, signalPos);
        synchronized (CURRENTLY_LOADED_STATES) {
            final Map<SEProperty, String> properties = CURRENTLY_LOADED_STATES
                    .computeIfAbsent(stateInfo, _u -> new HashMap<>());
            for (int i = 0; i < propertiesSize; i++) {
                final SEProperty property = signalProperties.get(propertyIDs[i]);
                final String value = property.getObjFromID(valueIDs[i]);
                properties.put(property, value);
            }
            CURRENTLY_LOADED_STATES.put(stateInfo, properties);
        }
        final long startTime = Calendar.getInstance().getTimeInMillis();
        SERVICE.execute(() -> {
            BlockEntity entity;
            while ((entity = level.getBlockEntity(signalPos)) == null) {
                final long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - startTime >= 5000) {
                    return;
                }
                continue;
            }
            final BlockState state = entity.getBlockState();
            mc.level.setBlocksDirty(signalPos, state, state);
            entity.requestModelDataUpdate();
            mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();
        synchronized (CURRENTLY_LOADED_STATES) {
            CURRENTLY_LOADED_STATES.remove(new ClientSignalStateInfo(mc.level, pos));
        }
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}