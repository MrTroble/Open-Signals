package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientSignalsStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);

    public static final Map<SEProperty, String> getClientStates(final SignalStateInfo info) {
        return CURRENTLY_LOADED_STATES.computeIfAbsent(info, _u -> new HashMap<>());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos signalPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final int propertiesSize = Byte.toUnsignedInt(buf.get());
        if (propertiesSize == 255) {
            // TODO inform client to unrender
            return;
        }
        final int[] propertyIDs = new int[propertiesSize];
        final int[] valueIDs = new int[propertiesSize];
        for (int i = 0; i < propertiesSize; i++) {
            propertyIDs[i] = Byte.toUnsignedInt(buf.get());
            valueIDs[i] = Byte.toUnsignedInt(buf.get());
        }
        SERVICE.execute(() -> {
            final Minecraft mc = Minecraft.getInstance();
            final Level level = mc.level;
            if (level == null)
                return;
            BlockEntity entity;
            while ((entity = level.getBlockEntity(signalPos)) == null)
                continue;
            final SignalStateInfo stateInfo = new SignalStateInfo(level, signalPos);
            final List<SEProperty> signalProperties = stateInfo.signal.getProperties();
            synchronized (CURRENTLY_LOADED_STATES) {
                final Map<SEProperty, String> properties = CURRENTLY_LOADED_STATES
                        .computeIfAbsent(stateInfo, _u -> new HashMap<>());
                for (int i = 0; i < propertiesSize; i++) {
                    final SEProperty property = signalProperties.get(propertyIDs[i]);
                    final String value = property.getObjFromID(valueIDs[i]);
                    properties.put(property, value);
                }
            }
            entity.requestModelDataUpdate();
            mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
        });
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}
