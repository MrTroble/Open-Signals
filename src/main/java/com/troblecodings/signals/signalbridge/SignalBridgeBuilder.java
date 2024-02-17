package com.troblecodings.signals.signalbridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.guilib.ecs.entitys.UIBlockRenderInfo;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.Vector3iHelper;
import com.troblecodings.signals.models.ModelInfoWrapper;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SignalBridgeBuilder {

    public static final ModelInfoWrapper EMPTY_WRAPPER = new ModelInfoWrapper(
            EmptyModelData.INSTANCE);

    public static final String SIGNALBRIDGE_BLOCKS = "signalBridgeBlocks";
    public static final String SIGNALS_ON_BRIDGE = "signalsOnBridge";
    public static final String VECTOR_X = "vectorX";
    public static final String VECTOR_Y = "vectorY";
    public static final String VECTOR_Z = "vectorZ";
    public static final String START_POINT = "startPoint";
    public static final String CUSTOMNAME = "signalCustomName";
    private static final Vector3i RENDER_START = new Vector3i(15, 15, 0);

    private final Map<Point, SignalBridgeBasicBlock> pointForBlocks = new HashMap<>();
    private final Map<Entry<String, Signal>, Vector3i> vecForSignal = new HashMap<>();
    private List<Map.Entry<Vector3i, BasicBlock>> relativesToStart = ImmutableList.of();
    private Function<String, ModelInfoWrapper> function = (_u) -> EMPTY_WRAPPER;
    private Point startPoint = new Point(13, 13);

    public void changeStartPoint(final Point newPoint) {
        if (newPoint.equals(startPoint))
            return;
        this.startPoint = newPoint;
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void addBlock(final Point point, final SignalBridgeBasicBlock block) {
        pointForBlocks.put(point, block);
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public void removeBridgeBlock(final Point point) {
        pointForBlocks.remove(point);
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public Vector3i addSignal(final Vector3i vec, final Signal signal, final String name) {
        final Entry<String, Signal> entry = Maps.immutableEntry(name, signal);
        if (vecForSignal.containsKey(entry))
            return vecForSignal.get(entry);
        vecForSignal.put(entry, vec);
        this.relativesToStart = calculateRelativesToPoint(startPoint);
        return vec;
    }

    public void setNewSignalPos(final Signal signal, final String name, final Vector3i vec) {
        final Entry<String, Signal> entry = Maps.immutableEntry(name, signal);
        vecForSignal.put(entry, vec);
    }

    public void removeSignal(final Entry<String, Signal> entry) {
        vecForSignal.remove(entry);
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public SignalBridgeBasicBlock getBlockOnPoint(final Point point) {
        return pointForBlocks.get(point);
    }

    public Vector3i getVecForSignal(final Entry<String, Signal> entry) {
        return vecForSignal.get(entry);
    }

    public boolean hasBlockOn(final Vector3i vec, final Entry<String, Signal> entry) {
        final boolean isCollidingWithBlock = vec.getZ() == 0
                ? pointForBlocks.containsKey(new Point(vec.getX(), vec.getY()))
                : false;
        final Vector3i signalVec = vecForSignal.get(entry);
        return isCollidingWithBlock || (!vec.equals(signalVec) && vecForSignal.containsValue(vec));
    }

    public void updateSignalName(final String oldName, final String newName, final Signal signal) {
        final Entry<String, Signal> oldEntry = Maps.immutableEntry(oldName, signal);
        final Entry<String, Signal> newEntry = Maps.immutableEntry(newName, signal);
        if (!vecForSignal.containsKey(oldEntry))
            return;
        vecForSignal.put(newEntry, vecForSignal.remove(oldEntry));
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public void setFunctionForModelData(final Function<String, ModelInfoWrapper> function) {
        this.function = function;
    }

    private List<Entry<Vector3i, BasicBlock>> calculateRelativesToPoint(final Point startPoint) {
        if (startPoint == null) {
            return new ArrayList<>();
        }
        final Builder<Map.Entry<Vector3i, BasicBlock>> builder = ImmutableList.builder();
        final Vector3i startVec = new Vector3i(startPoint.getX(), startPoint.getY(), 0);
        pointForBlocks.forEach((point, block) -> {
            final Vector3i vector = new Vector3i(point.getX(), point.getY(), 0);

            builder.add(Maps.immutableEntry(Vector3iHelper.subtract(startVec, vector), block));
        });
        vecForSignal.forEach((entry, vec) -> builder.add(
                Maps.immutableEntry(Vector3iHelper.subtract(startVec, vec), entry.getValue())));
        return builder.build();
    }

    public Map<Entry<String, Signal>, Vector3i> getAllSignalsInRelativeToStart() {
        if (startPoint == null) {
            return new HashMap<>();
        }
        final Vector3i startVec = new Vector3i(startPoint.getX(), startPoint.getY(), 0);
        final Map<Entry<String, Signal>, Vector3i> map = new HashMap<>();
        vecForSignal
                .forEach((entry, vec) -> map.put(entry, Vector3iHelper.subtract(startVec, vec)));
        return map;
    }

    public List<UIBlockRenderInfo> getRenderPosAndBlocks() {
        final Builder<UIBlockRenderInfo> builder = ImmutableList.builder();
        pointForBlocks.forEach((point, block) -> {
            final Vector3i vector = new Vector3i(point.getX(), point.getY(), 0);
            builder.add(new UIBlockRenderInfo(block.defaultBlockState(), EMPTY_WRAPPER,
                    Vector3iHelper.subtract(RENDER_START, vector)));
        });
        vecForSignal.forEach((entry,
                vec) -> builder.add(new UIBlockRenderInfo(entry.getValue().defaultBlockState(),
                        function.apply(entry.getKey()),
                        Vector3iHelper.subtract(RENDER_START, vec))));
        return builder.build();

    }

    public List<Entry<Vector3i, BasicBlock>> getRelativesToStart() {
        return relativesToStart;
    }

    public Map<Entry<String, Signal>, Vector3i> getAllSignals() {
        return ImmutableMap.copyOf(vecForSignal);
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
        vecForSignal.forEach((entry, vec) -> {
            final NBTWrapper tag = new NBTWrapper();
            tag.putInteger(VECTOR_X, vec.getX());
            tag.putInteger(VECTOR_Y, vec.getY());
            tag.putInteger(VECTOR_Z, vec.getZ());
            tag.putString(SIGNALS_ON_BRIDGE, entry.getValue().getRegistryName().getPath());
            tag.putString(CUSTOMNAME, entry.getKey());
            signalList.add(tag);
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
            final Vector3i vector = new Vector3i(tag.getInteger(VECTOR_X), tag.getInteger(VECTOR_Y),
                    tag.getInteger(VECTOR_Z));
            vecForSignal.put(Maps.immutableEntry(tag.getString(CUSTOMNAME),
                    (Signal) Registry.BLOCK.get(new ResourceLocation(OpenSignalsMain.MODID,
                            tag.getString(SIGNALS_ON_BRIDGE)))),
                    vector);
        });
        final NBTWrapper startPointWrapper = wrapper.getWrapper(START_POINT);
        if (!startPointWrapper.isTagNull()) {
            this.startPoint = new Point();
            this.startPoint.read(startPointWrapper);
        }
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }

    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) pointForBlocks.size());
        pointForBlocks.forEach((point, block) -> {
            point.writeNetwork(buffer);
            buffer.putInt(block.getID());
        });
        buffer.putByte((byte) vecForSignal.size());
        vecForSignal.forEach((entry, vec) -> {
            buffer.putString(entry.getKey());
            buffer.putInt(entry.getValue().getID());
            buffer.putInt(vec.getX());
            buffer.putInt(vec.getY());
            buffer.putInt(vec.getZ());
        });
        buffer.putBoolean(startPoint != null);
        if (startPoint != null)
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
            vecForSignal.put(
                    Maps.immutableEntry(buffer.getString(), Signal.SIGNAL_IDS.get(buffer.getInt())),
                    new Vector3i(buffer.getInt(), buffer.getInt(), buffer.getInt()));
        }
        if (buffer.getBoolean())
            this.startPoint = Point.of(buffer);
        this.relativesToStart = calculateRelativesToPoint(startPoint);
    }
}