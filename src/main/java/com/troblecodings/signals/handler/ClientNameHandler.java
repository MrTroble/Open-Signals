package com.troblecodings.signals.handler;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkEvent.ServerCustomPayloadEvent;

public class ClientNameHandler implements INetworkSync {

    private static final Map<StateInfo, String> CLIENT_NAMES = new HashMap<>();

    public static String getClientName(final StateInfo info) {
        synchronized (CLIENT_NAMES) {
            return CLIENT_NAMES.getOrDefault(info, "");
        }
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        final Minecraft mc = Minecraft.getInstance();
        final BlockPos pos = buffer.getBlockPos();
        final int byteLength = buffer.getByteToUnsignedInt();
        if (byteLength == 255) {
            setRemoved(pos);
            return;
        }
        final byte[] array = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            array[i] = buffer.getByte();
        }
        String name = "";
        try {
            name = new String(array, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.put(new StateInfo(mc.level, pos), name);
        }
        final ClientWorld world = mc.level;
        mc.submit(() -> {
            final BlockState state = world.getBlockState(pos);
            if (state == null)
                return;
            world.setBlocksDirty(pos, state, state);
            world.setBlockAndUpdate(pos, state);
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.remove(new StateInfo(mc.level, pos));
        }
    }

    @SubscribeEvent
    public void serverEvent(final ServerCustomPayloadEvent event) {
        deserializeClient(event.getPayload().nioBuffer());
        event.getSource().get().setPacketHandled(true);
    }
}