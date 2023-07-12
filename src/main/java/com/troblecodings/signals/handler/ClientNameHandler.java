package com.troblecodings.signals.handler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;

public class ClientNameHandler implements INetworkSync {

    private static final Map<NameStateInfo, String> CLIENT_NAMES = new HashMap<>();

    public static String getClientName(final NameStateInfo info) {
        synchronized (CLIENT_NAMES) {
            return CLIENT_NAMES.getOrDefault(info, "");
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final Minecraft mc = Minecraft.getMinecraft();
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
        final String name = new String(array);
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.put(new NameStateInfo(mc.world, pos), name);
        }
        final WorldClient level = mc.world;
        mc.addScheduledTask(() -> {
            level.getChunkFromBlockCoords(pos).markDirty();
            final IBlockState state = level.getBlockState(pos);
            mc.renderGlobal.notifyLightSet(pos);
            mc.renderGlobal.notifyBlockUpdate(level, pos, state, state, 8);
            level.notifyBlockUpdate(pos, state, state, 3);
        });
    }

    private static void setRemoved(final BlockPos pos) {
        final Minecraft mc = Minecraft.getMinecraft();
        synchronized (CLIENT_NAMES) {
            CLIENT_NAMES.remove(new NameStateInfo(mc.world, pos));
        }
    }

    @SubscribeEvent
    public void serverEvent(final ClientCustomPacketEvent event) {
        deserializeClient(event.getPacket().payload().nioBuffer());
    }
}