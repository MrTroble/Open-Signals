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
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerPlacementtool extends ContainerBase implements INetworkSync {

    private int signalID;
    public final Map<SEProperty, Integer> properties = new HashMap<>();
    private final Player player;
    private Signal signal;

    public ContainerPlacementtool(final GuiInfo info) {
        super(info);
        info.base = this;
        this.player = info.player;
        info.player.containerMenu = this;
    }

    @Override
    public void sendAllDataToRemote() {
        sendItemProperties(player);
    }

    private void sendItemProperties(final Player player) {
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
        final ByteBuffer buffer = ByteBuffer.allocate(propertiesToSend.size() + 5);
        buffer.putInt(signalID);
        buffer.put((byte) propertiesToSend.size());
        propertiesToSend.forEach(obj -> {
            buffer.put(obj);
        });
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final int first = Byte.toUnsignedInt(buf.get());
        final ItemStack stack = player.getMainHandItem();
        final Placementtool tool = (Placementtool) stack.getItem();
        if (first == 255) {
            final NBTWrapper wrapper = NBTWrapper.createForStack(stack);
            final int id = buf.getInt();
            wrapper.putInteger(Placementtool.BLOCK_TYPE_ID, id);
            this.signal = tool.getObjFromID(id);
            properties.clear();
            sendItemProperties(player);
        } else {
            final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
            final SEProperty property = signal.getProperties().get(first);
            final String value = property.getObjFromID(Byte.toUnsignedInt(buf.get()));
            if ((value.equals("false") || value.equals("OFF"))
                    && property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
                wrapper.remove(property.getName());
                return;
            }
            wrapper.putString(property.getName(), value);
        }
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        signalID = buf.getInt();
        final int size = Byte.toUnsignedInt(buf.get());
        final Placementtool tool = (Placementtool) player.getMainHandItem().getItem();
        final Signal signal = tool.getObjFromID(signalID);
        final List<SEProperty> signalProperties = signal.getProperties();
        properties.clear();
        for (int i = 0; i < size / 2; i++) {
            final SEProperty property = signalProperties.get(Byte.toUnsignedInt(buf.get()));
            final int value = Byte.toUnsignedInt(buf.get());
            properties.put(property, value);
        }
        signalProperties.forEach(property -> {
            if (!properties.containsKey(property)) {
                properties.put(property,
                        property.getParent().getIDFromValue(property.getDefault()));
            }
        });
        update();
    }

    public int getSignalID() {
        return signalID;
    }
}