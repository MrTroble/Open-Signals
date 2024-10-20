package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.enums.ShowSubsidiary;

import net.minecraft.core.BlockPos;

public class MainSignalIdentifier extends PosIdentifier {

    public SignalState state = SignalState.RED;

    public MainSignalIdentifier(final ModeIdentifier identifier, final BlockPos pos,
            final SignalState state) {
        super(identifier, pos);
        this.state = state;
    }

    public MainSignalIdentifier(final Point point, final ModeSet mode, final BlockPos pos) {
        super(point, mode, pos);
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        super.writeNetwork(buffer);
        buffer.putEnumValue(state);
    }

    public static MainSignalIdentifier of(final ReadBuffer buffer) {
        return new MainSignalIdentifier(ModeIdentifier.of(buffer), buffer.getBlockPos(),
                buffer.getEnumValue(SignalState.class));
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, pos, state);
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
        return Objects.equals(identifier, other.identifier) && Objects.equals(pos, other.pos)
                && state == other.state;
    }

    @Override
    public String toString() {
        return "MainSignalIdentifier [ModeIdentifier=" + identifier + ",pos=" + pos + ",state="
                + state + "]";
    }

    public static enum SignalState {

        RED, GREEN, OFF, SUBSIDIARY_GREEN, SUBSIDIARY_RED, SUBSIDIARY_OFF;

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