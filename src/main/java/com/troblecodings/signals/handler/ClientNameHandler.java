package com.troblecodings.signals.handler;

import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.StateInfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

public class ClientNameHandler implements INetworkSync {

    private static final Map<StateInfo, String> CLIENT_NAMES = new HashMap<>();

    public static String getClientName(final StateInfo info) {
        synchronized (CLIENT_NAMES) {
            return new String(CLIENT_NAMES.getOrDefault(info, ""));
        }
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        final Minecraft mc = Minecraft.getMinecraft();
        final BlockPos pos = buffer.getBlockPos();
        final boolean removed = buffer.getBoolean();
        if (removed) {
            setRemoved(pos);
            return;
        }
        final String name = buffer.getString();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.put(new StateInfo(mc.world, pos), name);
        }
        final WorldClient level = mc.world;
        if (level == null)
            return;
        mc.addScheduledTask(() -> {
            level.getChunkFromBlockCoords(pos).markDirty();
            final IBlockState state = level.getBlockState(pos);
            if (state == null)
                return;
            mc.renderGlobal.notifyLightSet(pos);
            mc.renderGlobal.notifyBlockUpdate(level, pos, state, state, 8);
            level.notifyBlockUpdate(pos, state, state, 3);
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.remove(new StateInfo(mc.world, pos));
        }
    }

    @SubscribeEvent
    public void serverEvent(final ClientCustomPacketEvent event) {
        deserializeClient(event.getPacket().payload().nioBuffer());
    }
}