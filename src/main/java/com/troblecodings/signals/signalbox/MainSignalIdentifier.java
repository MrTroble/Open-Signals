package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.enums.ShowSubsidiary;

import net.minecraft.util.math.BlockPos;

public class MainSignalIdentifier extends PosIdentifier {

    SignalBoxNode node = null;

    public MainSignalIdentifier(final ModeIdentifier identifier, final BlockPos pos,
            final SignalBoxGrid grid) {
        super(identifier, pos);
        this.node = grid.getNodeChecked(getPoint()).orElseThrow(() -> new IllegalArgumentException(
                "There should be a node for " + getPoint() + " in " + grid + " but there isin't!"));
    }

    public MainSignalIdentifier(final Point point, final ModeSet mode, final BlockPos pos,
            final SignalBoxGrid grid) {
        this(new ModeIdentifier(point, mode), pos, grid);
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        super.writeNetwork(buffer);
    }

    public static MainSignalIdentifier of(final ReadBuffer buffer, final SignalBoxGrid grid) {
        return new MainSignalIdentifier(ModeIdentifier.of(buffer), buffer.getBlockPos(), grid);
    }

    public void updateSignalState(final SignalState state) {
        node.updateState(getModeSet(), state);
    }

    public SignalState getSignalState() {
        return node.getState(getModeSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, pos);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MainSignalIdentifier other = (MainSignalIdentifier) obj;
        return Objects.equals(identifier, other.identifier) && Objects.equals(pos, other.pos);
    }

    @Override
    public String toString() {
        return "MainSignalIdentifier [ModeIdentifier=" + identifier + ",pos=" + pos + "]";
    }

    public static enum SignalState {

        GREEN, RED, OFF, SUBSIDIARY_GREEN, SUBSIDIARY_RED, SUBSIDIARY_OFF;

        public static SignalState combine(final ShowSubsidiary show) {
            if (show == null)
                return SUBSIDIARY_RED;
            switch (show) {
                case SIGNAL_RED:
                    return SUBSIDIARY_RED;
                case SIGNAL_GREEN:
                    return SUBSIDIARY_GREEN;
                default:
                    return SUBSIDIARY_OFF;
            }
        }

    }
}
