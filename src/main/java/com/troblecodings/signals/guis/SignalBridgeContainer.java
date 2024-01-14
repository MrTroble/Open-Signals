package com.troblecodings.signals.guis;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.enums.SignalBridgeNetwork;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbridge.SignalBridgeBasicBlock;
import com.troblecodings.signals.signalbridge.SignalBridgeBuilder;

import net.minecraft.core.Vec3i;

public class SignalBridgeContainer extends ContainerBase {

    public static final String SIGNALBRIDGE_TAG = "signalBridgeTag";

    protected final SignalBridgeBuilder builder = new SignalBridgeBuilder();

    public SignalBridgeContainer(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        final NBTWrapper itemTag = NBTWrapper.getOrCreateWrapper(info.player.getMainHandItem())
                .getWrapper(SIGNALBRIDGE_TAG);
        builder.read(itemTag == null ? new NBTWrapper() : itemTag);
        final WriteBuffer buffer = new WriteBuffer();
        builder.writeNetwork(buffer);
        OpenSignalsMain.network.sendTo(info.player, buffer);
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        builder.readNetwork(buf);
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
                final Vec3i vec = new Vec3i(buf.getInt(), buf.getInt(), buf.getInt());
                builder.addSignal(vec, Signal.SIGNAL_IDS.get(buf.getInt()));
                break;
            }
            case REMOVE_BLOCK: {
                final Point point = Point.of(buf);
                builder.removeBridgeBlock(point);
                break;
            }
            case REMOVE_SIGNAL: {
                final Vec3i vec = new Vec3i(buf.getInt(), buf.getInt(), buf.getInt());
                builder.removeSignal(vec);
            }
            default:
                break;
        }
        builder.write(bridgeTag);
        itemTag.putWrapper(SIGNALBRIDGE_TAG, bridgeTag);
    }
}
