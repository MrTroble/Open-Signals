package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.PropertyPacket;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerPlacementtool extends ContainerBase implements INetworkSync, PropertyPacket {

    private final Player player;
    private Signal signal;

    public ContainerPlacementtool(final GuiInfo info) {
        super(info);
        this.player = info.player;
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final int first = Byte.toUnsignedInt(buf.get());
        final ItemStack stack = player.getMainHandItem();
        final Placementtool tool = (Placementtool) stack.getItem();
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
        if (first == 255) {
            final int id = buf.getInt();
            wrapper.putInteger(Placementtool.BLOCK_TYPE_ID, id);
            this.signal = tool.getObjFromID(id);
            System.out.println("Signal: " + this.signal);
        } else {
            final SEProperty property = signal.getProperties().get(first);
            final String value = property.getObjFromID(Byte.toUnsignedInt(buf.get()));
            wrapper.putString(property.getName(), value);
            System.out.printf("Property change %s: %s %n", property, value);
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        // TODO Auto-generated method stub
        INetworkSync.super.deserializeClient(buf);
    }
}