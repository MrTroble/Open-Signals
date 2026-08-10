package com.troblecodings.signals.guis;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.ContainerBase;
import com.troblecodings.guilib.ecs.GuiInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Monitor;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class ContainerMonitorSelection extends ContainerBase {

    public static final String MONITOR_TYPE_ID = "monitorTypeID";
    public static final String SIZE_X = "monitorSizeX";
    public static final String SIZE_Y = "monitorSizeY";

    private static final byte SEND_BLOCK = 0;
    private static final byte SEND_SIZE = 1;

    protected int selectedMonitor, sizeX, sizeY;

    public ContainerMonitorSelection(final GuiInfo info) {
        super(info);
    }

    @Override
    public void sendAllDataToRemote() {
        final ItemStack stack = getPlayer().getHeldItemMainhand();
        final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);

        final WriteBuffer buffer = new WriteBuffer();
        buffer.putInt(wrapper.getInteger(MONITOR_TYPE_ID));
        buffer.putInt(wrapper.getInteger(SIZE_X));
        buffer.putInt(wrapper.getInteger(SIZE_Y));
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    @Override
    public void deserializeServer(final ReadBuffer buf) {
        final ItemStack stack = info.player.getHeldItemMainhand();
        final byte mode = buf.getByte();
        if (mode == SEND_BLOCK) {
            final NBTWrapper wrapper = NBTWrapper.createForStack(stack);
            final int blockID = buf.getInt();
            wrapper.putInteger(MONITOR_TYPE_ID, blockID);
        } else if (mode == SEND_SIZE) {
            final NBTWrapper wrapper = NBTWrapper.getOrCreateWrapper(stack);
            final EnumFacing.Axis axis = buf.getEnumValue(EnumFacing.Axis.class);
            final int size = buf.getInt();
            wrapper.putInteger(axis == EnumFacing.Axis.X ? SIZE_X : SIZE_Y, size);
        }
    }

    @Override
    public void deserializeClient(final ReadBuffer buf) {
        selectedMonitor = buf.getInt();
        sizeX = buf.getInt();
        sizeY = buf.getInt();
        update();
    }

    public void sendSelectedMonitorToServer(final Monitor monitor) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(SEND_BLOCK);
        buffer.putInt(monitor.getID());
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

    public void sendSizeToServer(final EnumFacing.Axis axis, final int value) {
        final WriteBuffer buffer = new WriteBuffer();
        buffer.putByte(SEND_SIZE);
        buffer.putEnumValue(axis);
        buffer.putInt(value);
        OpenSignalsMain.network.sendTo(getPlayer(), buffer);
    }

}
