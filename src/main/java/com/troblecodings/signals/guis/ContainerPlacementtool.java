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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

    private void sendItemProperties(final Player player) {
        final ItemStack stack = player.getMainHandItem();
        final Placementtool tool = (Placementtool) stack.getItem();
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
        final int signalID = wrapper.getInteger(Placementtool.BLOCK_TYPE_ID);
        signal = tool.getObjFromID(signalID);
        final List<SEProperty> properties = signal.getProperties().stream()
                .filter(property -> wrapper.contains(property.getName())).toList();
        final WriteBuffer buffer = new WriteBuffer();
        final List<Byte> propertiesToSend = new ArrayList<>();
        for (int i = 0; i < properties.size(); i++) {
            final SEProperty property = properties.get(i);
            if (wrapper.contains(property.getName())) {
                propertiesToSend.add((byte) i);
                final String value = wrapper.getString(property.getName());
                propertiesToSend.add((byte) property.getParent().getIDFromValue(value));
            }
        }
        buffer.putInt(signalID);
        buffer.putList(properties, (buf, prop) -> {
            buffer.putByte((byte) signal.getIDFromProperty(prop));
            final String value = wrapper.getString(prop.getName());
            buffer.putByte((byte) prop.getParent().getIDFromValue(value));
        });
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
        final Placementtool tool = (Placementtool) info.player.getMainHandItem().getItem();
        final Signal signal = tool.getObjFromID(signalID);
        final List<SEProperty> signalProperties = signal.getProperties();
        properties.clear();
        buffer.getList(buf -> {
            final SEProperty prop = signalProperties.get(buf.getByteToUnsignedInt());
            final int value = buf.getByteToUnsignedInt();
            properties.put(prop, value);
            return prop;
        });
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