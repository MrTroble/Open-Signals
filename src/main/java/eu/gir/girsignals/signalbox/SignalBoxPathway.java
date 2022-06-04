package eu.gir.girsignals.signalbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SignalBoxPathway implements ISaveable {

    private final ImmutableList<SignalBoxNode> listOfNodes;
    private final ConfigInfo configInfo;
    private final Map<BlockPos, Path> mapOfResetPositions = new HashMap<>();
    private final Map<BlockPos, Path> mapOfBlockingPositions = new HashMap<>();

    /**
     * Creates a new pathway
     * 
     * @param pNodes the nodes that are contained in this pathway
     */
    public SignalBoxPathway(final Iterable<SignalBoxNode> pNodes, final ConfigInfo configInfo) {
        this.listOfNodes = ImmutableList.copyOf(pNodes);
        this.configInfo = Objects.requireNonNull(configInfo);
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
