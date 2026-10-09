package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.NetworkBufferWrappers;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.parser.interm.LogicalSymbols;
import com.troblecodings.signals.tileentitys.SignalReaderTileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalReader extends ContainerBase {

    public ContainerSignalReader(final GuiInfo info) {
        super(info);
    }

    protected final Map<EnumFacing, Map.Entry<LogicalSymbols[], Map.Entry<SEProperty, String>[]>> statesForFace =
            new HashMap<>();
    protected final List<SEProperty> selectableProperties = new ArrayList<>();
    protected Signal signal;
    protected BlockPos pos;
    private SignalReaderTileEntity tile;

    @Override
    public void sendAllDataToRemote() {
        tile = info.getTile();
        if (!tile.hasLink())
            return;

        signal = tile.getLinkedSignalBlock();

        final Map<SEProperty, String> properties = SignalStateHandler
                .getStates(new SignalStateInfo(info.world, tile.getLinkedSignalPos(), signal));
        properties.forEach((property, value) -> {
            if ((property.isChangabelAtStage(ChangeableStage.APISTAGE)
                    || property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
                    && property.testMap(properties)) {
                selectableProperties.add(property);
            }
        });

        final WriteBuffer buffer = new WriteBuffer();
        buffer.putInt(signal.getID());
        buffer.putBlockPos(tile.getLinkedSignalPos());
        buffer.putMap(tile.getPropertiesForFace(), WriteBuffer.getEnumConsumer(),
                (buf, mainEntry) -> serializeEntries(mainEntry, buf));
        buffer.putList(selectableProperties, NetworkBufferWrappers.getSEPropertyConsumer(signal));
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        statesForFace.clear();
        selectableProperties.clear();
        signal = Signal.getSignalByID(buf.getInt());
        pos = buf.getBlockPos();
        buf.getMap(ReadBuffer.getEnumFunction(EnumFacing.class), buffer -> deserializeEntry(buffer))
                .forEach(statesForFace::put);
        buf.getList(NetworkBufferWrappers.getSEPropertyFunc(signal))
                .forEach(selectableProperties::add);
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final EnumFacing dir = buf.getEnumValue(EnumFacing.class);

        final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entry = deserializeEntry(buf);

        if (isArrayEmpty(entry.getKey()) && isArrayEmpty(entry.getValue())) {
            tile.removeDirection(dir);
        } else {
            tile.setUpForDirection(dir, Maps.immutableEntry(entry.getKey(), entry.getValue()));
        }
        tile.markDirty();
    }

    protected void sendToServerForDirection(final EnumFacing dir) {
        final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entries = statesForFace.get(dir);
        if (entries == null)
            return;
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(dir);
        serializeEntries(entries, buffer);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    private void serializeEntries(final Entry<LogicalSymbols[], Entry<SEProperty, String>[]> entry,
            final WriteBuffer buffer) {
        buffer.putInt(entry.getKey().length);
        for (final LogicalSymbols symbol : entry.getKey()) {
            buffer.putInt(symbol != null ? symbol.ordinal() : -1);
        }
        buffer.putInt(entry.getValue().length);
        for (final Entry<SEProperty, String> property : entry.getValue()) {
            if (property != null) {
                buffer.putInt(signal.getIDFromProperty(property.getKey()));
                buffer.putInt(property.getKey().getParent().getIDFromValue(property.getValue()));
            } else {
                buffer.putInt(-1);
            }
        }
    }

    private Entry<LogicalSymbols[], Entry<SEProperty, String>[]> deserializeEntry(
            final ReadBuffer buffer) {
        final int logicalSymbolLength = buffer.getInt();
        final LogicalSymbols[] symbols =
                new LogicalSymbols[SignalReaderTileEntity.MAX_LOGICAL_VALUES_SIZE];
        for (int i = 0; i < logicalSymbolLength; i++) {
            final int logicalSymbol = buffer.getInt();
            if (logicalSymbol != -1) {
                symbols[i] = LogicalSymbols.values()[logicalSymbol];
            }
        }

        final int propertiesLength = buffer.getInt();
        @SuppressWarnings("unchecked")
        final Entry<SEProperty, String>[] propertyEntries =
                new Entry[SignalReaderTileEntity.MAX_PROPERTIES_SIZE];
        for (int i = 0; i < propertiesLength; i++) {
            final int propertyID = buffer.getInt();
            if (propertyID != -1) {
                final SEProperty property = signal.getPropertyByIndex(propertyID);
                final String value = property.getObjFromID(buffer.getInt());
                propertyEntries[i] = Maps.immutableEntry(property, value);
            }
        }
        return Maps.immutableEntry(symbols, propertyEntries);
    }

    private static boolean isArrayEmpty(final Object[] array) {
        for (final Object obj : array) {
            if (obj != null)
                return false;
        }
        return true;
    }

}
