package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.troblecodings.signals.tileentitys.SignalReaderTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ContainerSignalReader extends ContainerBase {

    public ContainerSignalReader(final GuiInfo info) {
        super(info);
    }

    protected final Map<Direction, Map<SEProperty, String>> statesForFace = new HashMap<>();
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
                (propBuf, states) -> propBuf.putMap(states,
                        NetworkBufferWrappers.getSEPropertyConsumer(signal),
                        WriteBuffer.STRING_CONSUMER));
        buffer.putList(selectableProperties, NetworkBufferWrappers.getSEPropertyConsumer(signal));
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        statesForFace.clear();
        selectableProperties.clear();
        signal = Signal.getSignalByID(buf.getInt());
        pos = buf.getBlockPos();
        buf.getMap(ReadBuffer.getEnumFunction(Direction.class),
                mapBuf -> mapBuf.getMap(NetworkBufferWrappers.getSEPropertyFunc(signal),
                        ReadBuffer.STRING_FUNCTION))
                .forEach(statesForFace::put);
        buf.getList(NetworkBufferWrappers.getSEPropertyFunc(signal))
                .forEach(selectableProperties::add);
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final Direction dir = buf.getEnumValue(Direction.class);
        final SEProperty property = signal.getPropertyByIndex(buf.getInt());
        final int valueID = buf.getInt();
        if (valueID == -1) {
            tile.removePropertyFromDirection(dir, property);
        } else {
            tile.addPropertyToDirection(dir, property, property.getObjFromID(valueID));
        }
        tile.setChanged();
    }

    protected void sendPropertyToServer(final Direction dir, final SEProperty property,
            final int value) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putEnumValue(dir);
        buffer.putInt(signal.getIDFromProperty(property));
        buffer.putInt(value);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

}
