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
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.enums.SignalBridgeNetwork;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

public class ContainerSignalBridge extends ContainerBase {

    public static final String SIGNALBRIDGE_TAG = "signalBridgeTag";
    public static final String SIGNALPROPERTIES = "signalProperties";
    public static final String SIGNAL_NAME = "signalName";
    public static final String SIGNAL_ID = "signalId";

    protected final SignalBridgeBuilder builder = new SignalBridgeBuilder();
    protected final Map<String, Map.Entry<Signal, Map<SEProperty, Integer>>> allSignals = new HashMap<>();

    public ContainerSignalBridge(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        allSignals.clear();
        final NBTWrapper itemTag = NBTWrapper.getOrCreateWrapper(info.player.getMainHandItem())
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
        buffer.putMap(allSignals, WriteBuffer.STRING_CONSUMER, (buf, entry) -> {
            final Signal signal = entry.getKey();
            buffer.putInt(signal.getID());
            buffer.putMap(entry.getValue(), NetworkBufferWrappers.getSEPropertyConsumer(signal),
                    WriteBuffer.INT_TO_BYTE_CONSUMER);
        });
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        allSignals.clear();
        builder.readNetwork(buf);
        allSignals.putAll(buf.getMap(ReadBuffer.STRING_FUNCTION, (buffer) -> {
            final Signal signal = Signal.SIGNAL_IDS.get(buf.getInt());
            return Maps.immutableEntry(signal,
                    buffer.getMap(NetworkBufferWrappers.getSEPropertyFunc(signal),
                            ReadBuffer.BYTE_TO_INT_FUNCTION));
        }));
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final NBTWrapper itemTag = NBTWrapper.createForStack(info.player.getMainHandItem());
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
                final VectorWrapper vec = buf.getINetworkSaveable(VectorWrapper.class);
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