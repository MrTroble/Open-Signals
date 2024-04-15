package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkEvent.ServerCustomPayloadEvent;

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
        final boolean removed = buffer.getBoolean();
        if (removed) {
            setRemoved(pos);
            return;
        }
        final String name = buffer.getString();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.put(new StateInfo(mc.level, pos), name);
        }
        final ClientLevel world = mc.level;
        mc.doRunTask(() -> {
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