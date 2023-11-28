package com.troblecodings.signals.guis;

import java.util.ArrayList;
import java.util.List;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.tileentitys.BasicBlockEntity;
import com.troblecodings.signals.tileentitys.RedstoneIOTileEntity;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.util.math.BlockPos;

public class NamableContainer extends ContainerBase {

    protected BasicBlockEntity tile;
    protected BlockPos pos;
    protected final List<BlockPos> linkedPos = new ArrayList<>();
    protected final List<BlockPos> linkedController = new ArrayList<>();

    public NamableContainer(final GuiInfo info) {
        super(info);
        this.tile = info.getTile();
    }

    private void sendSignalPos() {
        final WriteBuffer buffer = new WriteBuffer();
        if (tile == null)
            tile = info.getTile();
        buffer.putBlockPos(info.pos);
        final List<BlockPos> linkedPos = tile.getLinkedPos();
        buffer.putByte((byte) linkedPos.size());
        linkedPos.forEach(pos -> buffer.putBlockPos(pos));
        if (tile instanceof RedstoneIOTileEntity) {
            final List<BlockPos> linkedController = ((RedstoneIOTileEntity) tile)
                    .getLinkedController();
            buffer.putByte((byte) linkedController.size());
            linkedController.forEach(pos -> buffer.putBlockPos(pos));
        }
        OpenSignalsMain.network.sendTo(info.player, buffer.build());
    }

    @Override
    public void sendAllDataToRemote() {
        sendSignalPos();
    }

    @Override
    public void deserializeClient(final ReadBuffer buffer) {
        linkedPos.clear();
        linkedController.clear();
        pos = buffer.getBlockPos();
        final int size = buffer.getByteToUnsignedInt();
        for (int i = 0; i < size; i++)
            linkedPos.add(buffer.getBlockPos());
        tile = (BasicBlockEntity) info.world.getTileEntity(pos);
        update();
    }

    @Override
    public void deserializeServer(final ReadBuffer buffer) {
        final StateInfo info = new StateInfo(this.info.world, this.info.pos);
        final String name = buffer.getString();
        if (tile instanceof SignalTileEntity) {
            NameHandler.setNameForSignal(info, name);
        } else {
            NameHandler.setNameForNonSignal(info, name);
        }
    }
}