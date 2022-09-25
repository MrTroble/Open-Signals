package eu.gir.girsignals.signalbox.debug;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxGrid;
import eu.gir.girsignals.signalbox.SignalBoxNode;
import eu.gir.girsignals.signalbox.SignalBoxPathway;
import net.minecraft.nbt.NBTTagCompound;

public class DebugGrid extends SignalBoxGrid {

    public DebugGrid(final Consumer<NBTTagCompound> sendToAll) {
        super(sendToAll);
    }

    @Override
    public void resetPathway(final Point p1) {
        final SignalBoxPathway pw = this.startsToPath.get(p1);
        if (pw == null)
            return;
        final List<SignalBoxNode> nodes = pw.getListOfNodes();
        GirsignalsMain.log.debug("Pathway prereset, {}", pw);
        GirsignalsMain.log.debug(nodes.stream().map(node -> node.toString())
                .collect(Collectors.joining(System.lineSeparator())));
        super.resetPathway(p1);
        GirsignalsMain.log.debug("Pathway postreset, {}", this.startsToPath.get(p1));
        GirsignalsMain.log.debug(nodes.stream().map(node -> node.toString())
                .collect(Collectors.joining(System.lineSeparator())));
    }
}
