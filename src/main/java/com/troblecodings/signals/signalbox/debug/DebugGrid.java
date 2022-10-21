package com.troblecodings.signals.signalbox.debug;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.signalbox.Point;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxNode;
import com.troblecodings.signals.signalbox.SignalBoxPathway;

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
        SignalsMain.log.debug("Pathway prereset, {}", pw);
        SignalsMain.log.debug(nodes.stream().map(node -> node.toString())
                .collect(Collectors.joining(System.lineSeparator())));
        super.resetPathway(p1);
        SignalsMain.log.debug("Pathway postreset, {}", this.startsToPath.get(p1));
        SignalsMain.log.debug(nodes.stream().map(node -> node.toString())
                .collect(Collectors.joining(System.lineSeparator())));
    }
}
