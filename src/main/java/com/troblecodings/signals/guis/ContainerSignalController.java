package com.troblecodings.signals.guis;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.troblecodings.core.interfaces.INetworkSync;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.guilib.ecs.interfaces.UIClientSync;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ContainerSignalController extends ContainerBase implements UIClientSync, INetworkSync {

    private final AtomicReference<Map<SEProperty, String>> reference = new AtomicReference<>();
    private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
    private final GuiInfo info;
    private Player player;
    private BlockPos linkedPos;

    public ContainerSignalController(final GuiInfo info) {
        super(info);
        info.base = this;
        info.player.containerMenu = this;
        this.info = info;
        this.player = info.player;
    }

    @Override
    public void sendAllDataToRemote() {
        sendProperitesToClient();
    }

    private void sendProperitesToClient() {
        if (info.pos == null) {
            return;
        }
        final SignalControllerTileEntity tile = (SignalControllerTileEntity) info.world
                .getBlockEntity(info.pos);
        linkedPos = tile.getLinkedPosition();
        if (linkedPos == null) {
            return;
        }
        final SignalStateInfo stateInfo = new SignalStateInfo(info.world, linkedPos);
        referenceBlock.set((Signal) info.world.getBlockState(linkedPos).getBlock());
        final Map<SEProperty, String> properties = SignalStateHandler.getStates(stateInfo);
        if (properties == null || properties.isEmpty())
            return;
        final ByteBuffer buffer = ByteBuffer.allocate(13 + (properties.size() * 2));
        buffer.putInt(stateInfo.pos.getX());
        buffer.putInt(stateInfo.pos.getY());
        buffer.putInt(stateInfo.pos.getZ());
        buffer.put((byte) properties.size());
        properties.forEach((property, value) -> {
            buffer.put((byte) stateInfo.signal.getIDFromProperty(property));
            buffer.put((byte) property.getParent().getIDFromValue(value));
        });
        OpenSignalsMain.network.sendTo(player, buffer);
    }

    @Override
    public void deserializeClient(final ByteBuffer buf) {
        final BlockPos pos = new BlockPos(buf.getInt(), buf.getInt(), buf.getInt());
        linkedPos = pos;
        final Signal signal = (Signal) info.world.getBlockState(pos).getBlock();
        referenceBlock.set(signal);
        final int size = Byte.toUnsignedInt(buf.get());
        final Map<SEProperty, String> properites = new HashMap<>();
        final List<SEProperty> signalProperites = signal.getProperties();
        for (int i = 0; i < size; i++) {
            final SEProperty property = signalProperites.get(Byte.toUnsignedInt(buf.get()));
            properites.put(property, property.getObjFromID(Byte.toUnsignedInt(buf.get())));
        }
        reference.set(properites);
        update();
    }

    @Override
    public void deserializeServer(final ByteBuffer buf) {
        final SEProperty property = getSignal().getProperties().get(Byte.toUnsignedInt(buf.get()));
        final String value = property.getObjFromID(Byte.toUnsignedInt(buf.get()));
        SignalStateHandler.setState(new SignalStateInfo(info.world, linkedPos), property, value);
    }

    public Map<SEProperty, String> getReference() {
        return reference.get();
    }

    public Signal getSignal() {
        return referenceBlock.get();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean stillValid(final Player playerIn) {
        if (playerIn instanceof Player) {
            this.player = playerIn;
        }
        return true;
    }

    public BlockPos getPos() {
        return linkedPos;
    }
}