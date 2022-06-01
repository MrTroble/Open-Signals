package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;

import eu.gir.girsignals.signalbox.entrys.ISaveable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SignalBoxPathway implements ISaveable {

    private final ImmutableSet<SignalBoxNode> listOfNodes;
    private final Map<BlockPos, Path> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, Path> mapOfBlockingPositions = new HashMap<>();

    /**
     * Creates a new pathway
     * 
     * @param pNodes the nodes that are contained in this pathway
     */
    public SignalBoxPathway(final Iterable<SignalBoxNode> pNodes) {
        listOfNodes = ImmutableSet.copyOf(pNodes);
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
}
