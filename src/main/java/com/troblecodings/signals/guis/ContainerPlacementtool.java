package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ContainerPlacementtool extends ContainerBase {

    public static final String SIGNAL_NAME = "signalName";

    protected final Map<SEProperty, Integer> properties = new HashMap<>();
    protected int signalID;
    protected String signalName = "";
    private Signal signal;

    public ContainerPlacementtool(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        sendItemProperties(info.player);
    }

    private void sendItemProperties(final PlayerEntity player) {
        final ItemStack stack = player.getMainHandItem();
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
        propertiesToSend.forEach(buffer::putByte);
        final String signalName = wrapper.getString(SIGNAL_NAME);
        buffer.putString(signalName);
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final int first = buffer.getByteToUnsignedInt();
        final ItemStack stack = info.player.getMainHandItem();
        final Placementtool tool = (Placementtool) stack.getItem();
        if (first == 255) {
            final int id = buffer.getInt();
            if (id == -1) {
                final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
                wrapper.putString(SIGNAL_NAME, buffer.getString());
                return;
            }
            final NBTWrapper wrapper = NBTWrapper.createForStack(stack);
            wrapper.putInteger(Placementtool.BLOCK_TYPE_ID, id);
            this.signal = tool.getObjFromID(id);
            properties.clear();
            sendItemProperties(info.player);
        } else {
            final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
            final SEProperty property = signal.getProperties().get(first);
            final String value = property.getObjFromID(buffer.getByteToUnsignedInt());
            if (property.getDefault().equals(value)) {
                wrapper.remove(property.getName());
                return;
            }
            wrapper.putString(property.getName(), value);
        }
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        signalID = buffer.getInt();
        final int size = buffer.getByteToUnsignedInt();
        final Placementtool tool = (Placementtool) info.player.getMainHandItem().getItem();
        final Signal signal = tool.getObjFromID(signalID);
        final List<SEProperty> signalProperties = signal.getProperties();
        properties.clear();
        for (int i = 0; i < size / 2; i++) {
            final SEProperty property = signalProperties.get(buffer.getByteToUnsignedInt());
            final int value = buffer.getByteToUnsignedInt();
            properties.put(property, value);
        }
        signalName = buffer.getString();
        signalProperties.forEach(property -> {
            if (!properties.containsKey(property)) {
                properties.put(property,
                        property.getParent().getIDFromValue(property.getDefault()));
            }
        });
        update();
    }
}