package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerPlacementtool extends ContainerBase implements INetworkSync {

    public static final String SIGNAL_NAME = "signalName";

    public final Map<SEProperty, Integer> properties = new HashMap<>();
    protected int signalID;
    protected String signalName = "";
    private final EntityPlayer player;
    private Signal signal;

    public ContainerPlacementtool(final GuiInfo info) {
        super(info);
        info.base = this;
        this.player = info.player;
        info.player.openContainer = this;
    }

    @Override
    public void sendAllDataToRemote() {
        sendItemProperties(player);
    }

    private void sendItemProperties(final EntityPlayer player) {
        final ItemStack stack = player.getHeldItemMainhand();
        final Placementtool tool = (Placementtool) stack.getItem();
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
        final int signalID = wrapper.getInteger(Placementtool.BLOCK_TYPE_ID);
        signal = tool.getObjFromID(signalID);
        final List<SEProperty> properites = signal.getProperties();
        final List<Byte> propertiesToSend = new ArrayList<>();
        for (int i = 0; i < properites.size(); i++) {
            final SEProperty property = properites.get(i);
            if (wrapper.contains(property.getName())) {
                propertiesToSend.add((byte) i);
                final String value = wrapper.getString(property.getName());
                propertiesToSend.add((byte) property.getParent().getIDFromValue(value));
            }
        }
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putInt(signalID);
        buffer.putByte((byte) propertiesToSend.size());
        propertiesToSend.forEach(obj -> {
            buffer.putByte(obj);
        });
        final String signalName = wrapper.getString(SIGNAL_NAME);
        final byte[] allBytes = signalName.getBytes();
        buffer.putByte((byte) allBytes.length);
        for (final byte b : allBytes) {
            buffer.putByte(b);
        }
        OpenSignalsMain.network.sendTo(player, buffer.build());
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        final int first = buffer.getByteAsInt();
        final ItemStack stack = player.getHeldItemMainhand();
        final Placementtool tool = (Placementtool) stack.getItem();
        if (first == 255) {
            final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
            final int id = buffer.getInt();
            if (id == -1) {
                final int nameSize = buffer.getByteAsInt();
                final byte[] name = new byte[nameSize];
                for (int i = 0; i < nameSize; i++) {
                    name[i] = buffer.getByte();
                }
                wrapper.putString(SIGNAL_NAME, new String(name));
                return;
            }
            wrapper.putInteger(Placementtool.BLOCK_TYPE_ID, id);
            this.signal = tool.getObjFromID(id);
            properties.clear();
            sendItemProperties(player);
        } else {
            final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
            final SEProperty property = signal.getProperties().get(first);
            final String value = property.getObjFromID(buffer.getByteAsInt());
            if (property.getDefault().equals(value)) {
                wrapper.remove(property.getName());
                return;
            }
            wrapper.putString(property.getName(), value);
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final ReadBuffer buffer = new ReadBuffer(buf);
        signalID = buffer.getInt();
        final int size = buffer.getByteAsInt();
        final Placementtool tool = (Placementtool) player.getHeldItemMainhand().getItem();
        final Signal signal = tool.getObjFromID(signalID);
        final List<SEProperty> signalProperties = signal.getProperties();
        properties.clear();
        for (int i = 0; i < size / 2; i++) {
            final SEProperty property = signalProperties.get(buffer.getByteAsInt());
            final int value = buffer.getByteAsInt();
            properties.put(property, value);
        }
        final int nameSize = buffer.getByteAsInt();
        final byte[] name = new byte[nameSize];
        for (int i = 0; i < nameSize; i++) {
            name[i] = buffer.getByte();
        }
        signalName = new String(name);
        signalProperties.forEach(property -> {
            if (!properties.containsKey(property)) {
                properties.put(property,
                        property.getParent().getIDFromValue(property.getDefault()));
            }
        });
        update();
    }
}