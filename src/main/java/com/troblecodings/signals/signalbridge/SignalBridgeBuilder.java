package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

public class SignalBridgeBuilder {

    public static final String SIGNALBRIDGE_BLOCKS = "signalBridgeBlocks";
    public static final String SIGNALS_ON_BRIDGE = "signalsOnBridge";
    public static final String VECTOR_X = "vectorX";
    public static final String VECTOR_Y = "vectorY";
    public static final String VECTOR_Z = "vectorZ";

    private final Map<Point, SignalBridgeBasicBlock> pointForBlocks = new HashMap<>();
    private final Map<Vec3i, Signal> vecForSignal = new HashMap<>();
    private Point startPoint = null;

    public void changeStartPoint(final Point newPoint) {
        this.startPoint = newPoint;
    }

    public void addBlock(final Point point, final SignalBridgeBasicBlock block) {
        pointForBlocks.put(point, block);
    }

    public void removeBridgeBlock(final Point point) {
        pointForBlocks.remove(point);
    }

    public void addSignal(final Vec3i vec, final Signal signal) {
        vecForSignal.put(vec, signal);
    }

    public void removeSignal(final Vec3i vec) {
        vecForSignal.remove(vec);
    }

    public SignalBridgeBasicBlock getBlockOnPoint(final Point point) {
        return pointForBlocks.get(point);
    }

    public Signal getSignalForVec(final Vec3i vector) {
        return vecForSignal.get(vector);
    }

    public List<Map.Entry<Vec3i, BasicBlock>> getRelativesToStart() {
        final List<Map.Entry<Vec3i, BasicBlock>> relativeToStart = new ArrayList<>();
        if (startPoint == null)
            return relativeToStart;
        final Vec3i startVec = new Vec3i(startPoint.getX(), startPoint.getY(), 0);
        pointForBlocks.forEach((point, block) -> {
            final Vec3i vector = new Vec3i(point.getX(), point.getY(), 0);
            relativeToStart.add(Maps.immutableEntry(startVec.subtract(vector), block));
        });
        vecForSignal.forEach((vec, signal) -> relativeToStart
                .add(Maps.immutableEntry(startVec.subtract(vec), signal)));
        return relativeToStart;
    }

    public void write(final NBTWrapper wrapper) {
        final List<NBTWrapper> blockList = new ArrayList<>();
        pointForBlocks.forEach((point, block) -> {
            final NBTWrapper tag = new NBTWrapper();
            point.write(tag);
            tag.putString(SIGNALBRIDGE_BLOCKS, block.getRegistryName().getPath());
            blockList.add(tag);
        });
        final List<NBTWrapper> signalList = new ArrayList<>();
        vecForSignal.forEach((vec, block) -> {
            final NBTWrapper tag = new NBTWrapper();
            tag.putInteger(VECTOR_X, vec.getX());
            tag.putInteger(VECTOR_Y, vec.getY());
            tag.putInteger(VECTOR_Z, vec.getZ());
            tag.putString(SIGNALS_ON_BRIDGE, block.getRegistryName().getPath());
        });
        wrapper.putList(SIGNALBRIDGE_BLOCKS, blockList);
        wrapper.putList(SIGNALS_ON_BRIDGE, signalList);
    }

    @SuppressWarnings("deprecation")
    public void read(final NBTWrapper wrapper) {
        pointForBlocks.clear();
        vecForSignal.clear();
        if (wrapper.isTagNull())
            return;
        wrapper.getList(SIGNALBRIDGE_BLOCKS).forEach(tag -> {
            final Point point = new Point();
            point.read(tag);
            pointForBlocks.put(point,
                    (SignalBridgeBasicBlock) Registry.BLOCK.get(new ResourceLocation(
                            OpenSignalsMain.MODID, tag.getString(SIGNALBRIDGE_BLOCKS))));
        });
        wrapper.getList(SIGNALS_ON_BRIDGE).forEach(tag -> {
            final Vec3i vector = new Vec3i(tag.getInteger(VECTOR_X), tag.getInteger(VECTOR_Y),
                    tag.getInteger(VECTOR_Z));
            vecForSignal.put(vector, (Signal) Registry.BLOCK.get(
                    new ResourceLocation(OpenSignalsMain.MODID, tag.getString(SIGNALS_ON_BRIDGE))));
        });
    }

    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) pointForBlocks.size());
        pointForBlocks.forEach((point, block) -> {
            point.writeNetwork(buffer);
            buffer.putInt(block.getID());
        });
        buffer.putByte((byte) vecForSignal.size());
        vecForSignal.forEach((vec, signal) -> {
            buffer.putInt(vec.getX());
            buffer.putInt(vec.getY());
            buffer.putInt(vec.getZ());
            buffer.putInt(signal.getID());
        });
    }

    public void readNetwork(final ReadBuffer buffer) {
        pointForBlocks.clear();
        vecForSignal.clear();
        final int blockSize = buffer.getByteToUnsignedInt();
        for (int i = 0; i < blockSize; i++) {
            pointForBlocks.put(Point.of(buffer),
                    SignalBridgeBasicBlock.ALL_SIGNALBRIDGE_BLOCKS.get(buffer.getInt()));
        }
        final int signalsSize = buffer.getByteToUnsignedInt();
        for (int i = 0; i < signalsSize; i++) {
            vecForSignal.put(new Vec3i(buffer.getInt(), buffer.getInt(), buffer.getInt()),
                    Signal.SIGNAL_IDS.get(buffer.getInt()));
        }
    }
}
