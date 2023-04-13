package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.core.BufferFactory;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientSignalsStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);

    public static final Map<SEProperty, String> getClientStates(final ClientSignalStateInfo info) {
        return CURRENTLY_LOADED_STATES.computeIfAbsent(info, _u -> new HashMap<>());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BufferFactory buffer = new BufferFactory(buf);
        final Minecraft mc = Minecraft.getInstance();
        final Level level = mc.level;
        final BlockPos signalPos = buffer.getBlockPos();
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
        SERVICE.execute(() -> {
            if (level == null)
                return;
            BlockEntity entity;
            while ((entity = level.getBlockEntity(signalPos)) == null)
                continue;
            final ClientSignalStateInfo stateInfo = new ClientSignalStateInfo(level, signalPos);
            final List<SEProperty> signalProperties = ((SignalTileEntity) entity).getSignal()
                    .getProperties();
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
            entity.requestModelDataUpdate();
            mc.levelRenderer.blockChanged(null, signalPos, null, null, 8);
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();
        SERVICE.execute(() -> {
            synchronized (CURRENTLY_LOADED_STATES) {
                CURRENTLY_LOADED_STATES.remove(new ClientSignalStateInfo(mc.level, pos));
            }
        });
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}
