package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.VectorWrapper;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.SignalBridgeNetwork;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

public class SignalBridgeContainer extends ContainerBase {

    public static final String SIGNALBRIDGE_TAG = "signalBridgeTag";
    public static final String SIGNALPROPERTIES = "signalProperties";
    public static final String SIGNAL_NAME = "signalName";
    public static final String SIGNAL_ID = "signalId";

    protected final SignalBridgeBuilder builder = new SignalBridgeBuilder();
    protected final Map<String, Map.Entry<Signal, Map<SEProperty, Integer>>> allSignals = new HashMap<>();

    public SignalBridgeContainer(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        allSignals.clear();
        final NBTWrapper itemTag = NBTWrapper.getOrCreateWrapper(info.player.getHeldItemMainhand())
                .getWrapper(SIGNALBRIDGE_TAG);
        builder.read(itemTag.isTagNull() ? new NBTWrapper() : itemTag);
        (itemTag.isTagNull() ? new NBTWrapper() : itemTag).getList(SIGNALPROPERTIES)
                .forEach(wrapper -> {
                    final Map<SEProperty, Integer> properties = new HashMap<>();
                    final String name = wrapper.getString(SIGNAL_NAME);
                    final Signal signal = Signal.SIGNALS.get(wrapper.getString(SIGNAL_ID));
                    signal.getProperties().forEach(
                            property -> property.readFromNBT(wrapper).ifPresent(value -> properties
                                    .put(property, property.getParent().getIDFromValue(value))));
                    allSignals.put(name, Maps.immutableEntry(signal, properties));
                });
        final WriteBuffer buffer = new WriteBuffer();
        builder.writeNetwork(buffer);
        buffer.putByte((byte) allSignals.size());
        allSignals.forEach((name, entry) -> {
            buffer.putString(name);
            buffer.putInt(entry.getKey().getID());
            final Map<SEProperty, Integer> properties = entry.getValue();
            buffer.putByte((byte) properties.size());
            properties.forEach((property, value) -> {
                buffer.putByte((byte) entry.getKey().getIDFromProperty(property));
                buffer.putByte(value.byteValue());
            });
        });
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        allSignals.clear();
        builder.readNetwork(buf);
        final int signalSize = buf.getByteToUnsignedInt();
        for (int i = 0; i < signalSize; i++) {
            final String name = buf.getString();
            final Signal signal = Signal.SIGNAL_IDS.get(buf.getInt());
            final int propertiesSize = buf.getByteToUnsignedInt();
            final Map<SEProperty, Integer> properties = new HashMap<>();
            final List<SEProperty> signalProperties = signal.getProperties();
            for (int j = 0; j < propertiesSize; j++) {
                final SEProperty property = signalProperties.get(buf.getByteToUnsignedInt());
                final int value = buf.getByteToUnsignedInt();
                properties.put(property, value);
            }
            allSignals.put(name, Maps.immutableEntry(signal, properties));
        }
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final NBTWrapper itemTag = NBTWrapper.createForStack(info.player.getHeldItemMainhand());
        final NBTWrapper bridgeTag = itemTag.contains(SIGNALBRIDGE_TAG)
                ? itemTag.getWrapper(SIGNALBRIDGE_TAG)
                : new NBTWrapper();
        final SignalBridgeNetwork mode = buf.getEnumValue(SignalBridgeNetwork.class);
        switch (mode) {
            case SET_BLOCK: {
                final Point point = Point.of(buf);
                builder.addBlock(point,
                        SignalBridgeBasicBlock.ALL_SIGNALBRIDGE_BLOCKS.get(buf.getInt()));
                break;
            }
            case SET_SIGNAL: {
                final VectorWrapper vec = VectorWrapper.of(buf);
                builder.setNewSignalPos(Signal.SIGNAL_IDS.get(buf.getInt()), buf.getString(), vec);
                break;
            }
            case REMOVE_BLOCK: {
                final Point point = Point.of(buf);
                builder.removeBridgeBlock(point);
                break;
            }
            case REMOVE_SIGNAL: {
                final Signal signal = Signal.SIGNAL_IDS.get(buf.getInt());
                final String name = buf.getString();
                builder.removeSignal(Maps.immutableEntry(name, signal));
                break;
            }
            case SEND_START_POINT: {
                final Point point = Point.of(buf);
                builder.changeStartPoint(point);
                break;
            }
            case SEND_CREATE_SIGNAL: {
                final String name = buf.getString();
                final Signal signal = Signal.SIGNAL_IDS.get(buf.getInt());
                allSignals.put(name, Maps.immutableEntry(signal, new HashMap<>()));
                break;
            }
            case SEND_PROPERTY: {
                final String signalName = buf.getString();
                final Map.Entry<Signal, Map<SEProperty, Integer>> entry = allSignals
                        .get(signalName);
                final SEProperty property = entry.getKey().getProperties()
                        .get(buf.getByteToUnsignedInt());
                final int value = buf.getByteToUnsignedInt();
                entry.getValue().put(property, value);
                break;
            }
            case REMOVE_SIGNAL_FROM_LIST: {
                final String name = buf.getString();
                allSignals.remove(name);
                break;
            }
            case CHANGE_NAME: {
                final String previous = buf.getString();
                final String newName = buf.getString();
                final Entry<Signal, Map<SEProperty, Integer>> entry = allSignals.remove(previous);
                allSignals.put(newName, entry);
                builder.updateSignalName(previous, newName, entry.getKey());
                break;
            }
            default:
                break;
        }
        builder.write(bridgeTag);
        final List<NBTWrapper> signals = new ArrayList<>();
        allSignals.forEach((name, entry) -> {
            final NBTWrapper propertiesWrapper = new NBTWrapper();
            propertiesWrapper.putString(SIGNAL_NAME, name);
            propertiesWrapper.putString(SIGNAL_ID, entry.getKey().getSignalTypeName());
            final Map<SEProperty, Integer> properties = entry.getValue();
            properties.forEach((property, value) -> property.writeToNBT(propertiesWrapper,
                    property.getParent().getObjFromID(value)));
            signals.add(propertiesWrapper);
        });
        bridgeTag.putList(SIGNALPROPERTIES, signals);
        itemTag.putWrapper(SIGNALBRIDGE_TAG, bridgeTag);
    }
}