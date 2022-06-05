package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalBoxPathway implements ISaveable, IChunkloadable {

    private final ImmutableList<SignalBoxNode> listOfNodes;
    private final PathType type;
    private final int speed;
    private final Map<BlockPos, SignalBoxNode> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, SignalBoxNode> mapOfBlockingPositions = new HashMap<>();

    /**
     * Creates a new pathway
     * 
     * @param pNodes the nodes that are contained in this pathway
     */
    public SignalBoxPathway(final List<SignalBoxNode> pNodes, final PathType type) {
        this.listOfNodes = ImmutableList.copyOf(pNodes);
        this.type = Objects.requireNonNull(type);
        final AtomicInteger atomic = new AtomicInteger(Integer.MAX_VALUE);
        foreachEntry((optionEntry, node) -> {
            optionEntry.getEntry(PathEntryType.SPEED)
                    .ifPresent(value -> atomic.updateAndGet(in -> Math.min(in, value)));
            optionEntry.getEntry(PathEntryType.BLOCKING)
                    .ifPresent(position -> mapOfBlockingPositions.put(position, node));
            optionEntry.getEntry(PathEntryType.RESETING)
                    .ifPresent(position -> mapOfResetPositions.put(position, node));
        });
        this.speed = atomic.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final NBTTagCompound tag) {
        listOfNodes.forEach(node -> node.write(tag));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final NBTTagCompound tag) {
        listOfNodes.forEach(node -> node.read(tag));
    }

    private void foreachEntry(final Consumer<PathOptionEntry> consumer) {
        foreachEntry(consumer, null);
    }

    private void foreachEntry(final Consumer<PathOptionEntry> consumer,
            final @Nullable Point point) {
        foreachEntry((entry, _u) -> consumer.accept(entry), point);
    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer) {
        foreachEntry(consumer, null);
    }

    private void foreachEntry(final BiConsumer<PathOptionEntry, SignalBoxNode> consumer,
            final @Nullable Point point) {
        for (int i = 1; i < listOfNodes.size() - 1; i++) {
            final Point oldPos = listOfNodes.get(i - 1).getPoint();
            final Point newPos = listOfNodes.get(i + 1).getPoint();
            final SignalBoxNode current = listOfNodes.get(i);
            if (current.getPoint().equals(point))
                break;
            current.getOption(new Path(oldPos, newPos))
                    .ifPresent(entry -> consumer.accept(entry, current));
        }
    }

    public void setPathStatus(final @Nullable World world, final EnumPathUsage status,
            final @Nullable Point point) {
        final WorldLoadOperations loadOps = new WorldLoadOperations(world);
        foreachEntry(option -> {
            option.getEntry(PathEntryType.OUTPUT)
                    .ifPresent(pos -> loadOps.setPower(pos, !status.equals(EnumPathUsage.FREE)));
            option.setEntry(PathEntryType.PATHUSAGE, status);
        }, point);
    }

    public void setPathStatus(final @Nullable World world, final EnumPathUsage status) {
        setPathStatus(world, status, null);
    }

}
