package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientSignalsStateHandler implements INetworkSync {

    private static final Map<SignalStateInfo, Map<SEProperty, String>> CURRENTLY_LOADED_STATES = new HashMap<>();

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(2);
    private static final ExecutorService REMOVE_SERVICE = Executors.newFixedThreadPool(3);

    public static final Map<SEProperty, String> getClientStates(final SignalStateInfo info) {
        return CURRENTLY_LOADED_STATES.computeIfAbsent(info, _u -> new HashMap<>());
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final Minecraft mc = Minecraft.getInstance();
        final Level level = mc.level;
        final BlockPos signalPos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        final int propertiesSize = Byte.toUnsignedInt(buf.get());
        if (propertiesSize == 255) {
            setRemoved(signalPos);
            return;
        }
        final int[] propertyIDs = new int[propertiesSize];
        final int[] valueIDs = new int[propertiesSize];
        for (int i = 0; i < propertiesSize; i++) {
            propertyIDs[i] = Byte.toUnsignedInt(buf.get());
            valueIDs[i] = Byte.toUnsignedInt(buf.get());
        }
        SERVICE.execute(() -> {
            if (level == null)
                return;
            BlockEntity entity;
            while ((entity = level.getBlockEntity(signalPos)) == null)
                continue;
            final SignalStateInfo stateInfo = new SignalStateInfo(level, signalPos,
                    ((SignalTileEntity) entity).getSignal());
            final List<SEProperty> signalProperties = stateInfo.signal.getProperties();
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
        REMOVE_SERVICE.execute(() -> {
            BlockState state;
            while ((state = mc.level.getBlockState(pos)) == null)
                continue;
            // final Signal signal = (Signal) state.getBlock();
            // CURRENTLY_LOADED_STATES.remove(new SignalStateInfo(mc.level, pos, signal));
        });
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}
