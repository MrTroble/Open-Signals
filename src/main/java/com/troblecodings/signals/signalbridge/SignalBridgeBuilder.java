package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
    public static final String START_POINT = "startPoint";

    private final Map<Point, SignalBridgeBasicBlock> pointForBlocks = new HashMap<>();
    private final Map<Vec3i, Signal> vecForSignal = new HashMap<>();
    private List<Map.Entry<Vec3i, BasicBlock>> relativesToStart = ImmutableList.of();
    private Point startPoint = new Point(-1, -1);

    public void changeStartPoint(final Point newPoint) {
        if (newPoint.equals(startPoint))
            return;
        this.startPoint = newPoint;
        updateRelativesToStart();
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void addBlock(final Point point, final SignalBridgeBasicBlock block) {
        pointForBlocks.put(point, block);
        updateRelativesToStart();
    }

    public void removeBridgeBlock(final Point point) {
        pointForBlocks.remove(point);
        updateRelativesToStart();
    }

    public void addSignal(final Vec3i vec, final Signal signal) {
        vecForSignal.put(vec, signal);
        updateRelativesToStart();
    }

    public void removeSignal(final Vec3i vec) {
        vecForSignal.remove(vec);
        updateRelativesToStart();
    }

    public SignalBridgeBasicBlock getBlockOnPoint(final Point point) {
        return pointForBlocks.get(point);
    }

    public Signal getSignalForVec(final Vec3i vector) {
        return vecForSignal.get(vector);
    }

    private void updateRelativesToStart() {
        if (startPoint == null) {
            this.relativesToStart = ImmutableList.of();
            return;
        }
        final Builder<Map.Entry<Vec3i, BasicBlock>> builder = ImmutableList.builder();
        final Vec3i startVec = new Vec3i(startPoint.getX(), startPoint.getY(), 0);
        pointForBlocks.forEach((point, block) -> {
            final Vec3i vector = new Vec3i(point.getX(), point.getY(), 0);
            builder.add(Maps.immutableEntry(startVec.subtract(vector), block));
        });
        vecForSignal.forEach(
                (vec, signal) -> builder.add(Maps.immutableEntry(startVec.subtract(vec), signal)));
        this.relativesToStart = builder.build();
    }

    public List<Map.Entry<Vec3i, BasicBlock>> getRelativesToStart() {
        return relativesToStart;
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
        if (startPoint != null) {
            final NBTWrapper startPointNBT = new NBTWrapper();
            startPoint.write(startPointNBT);
            wrapper.putWrapper(START_POINT, startPointNBT);
        }
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
        final NBTWrapper startPointWrapper = wrapper.getWrapper(START_POINT);
        if (!startPointWrapper.isTagNull()) {
            this.startPoint = new Point();
            this.startPoint.read(startPointWrapper);
        }
        updateRelativesToStart();
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
        startPoint.writeNetwork(buffer);
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
        this.startPoint = Point.of(buffer);
        if (this.startPoint.equals(new Point(-1, -1)))
            this.startPoint = null;
        updateRelativesToStart();
    }
}
