package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientNameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> CLIENT_NAMES = new HashMap<>();
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    public static String getClientName(final NameStateInfo info) {
        synchronized (CLIENT_NAMES) {
            final String name = CLIENT_NAMES.get(info);
            if (name == null)
                return "";
            return name;
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final Minecraft mc = Minecraft.getInstance();
        final ReadBuffer buffer = new ReadBuffer(buf);
        final BlockPos pos = buffer.getBlockPos();
        final int byteLength = buffer.getByteAsInt();
        if (byteLength == 255) {
            setRemoved(pos);
            return;
        }
        final byte[] array = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            array[i] = buffer.getByte();
        }
        final Level world = mc.level;
        final String name = new String(array);
        final long startTime = Calendar.getInstance().getTimeInMillis();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.put(new NameStateInfo(mc.level, pos), name);
        }
        SERVICE.execute(() -> {
            BlockEntity tile;
            while ((tile = world.getBlockEntity(pos)) == null) {
                final long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - startTime >= 10000) {
                    return;
                }
                continue;
            }
            if (tile instanceof BasicBlockEntity) {
                ((BasicBlockEntity) tile).setCustomName(name);
            }
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.remove(new NameStateInfo(mc.level, pos));
        }
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }

}
