package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.signalbox.ModeSet;
import com.troblecodings.signals.signalbox.Point;

import net.minecraft.util.math.BlockPos;

public class PosIdentifier {

    private static final String BLOCK_POS = "block_pos";

    public final ModeIdentifier identifier;
    public final BlockPos pos;

    public PosIdentifier(final Point point, final ModeSet mode, final BlockPos pos) {
        this(new ModeIdentifier(point, mode), pos);
    }

    public PosIdentifier(final ModeIdentifier identifier, final BlockPos pos) {
        this.identifier = identifier;
        this.pos = pos;
    }

    public void writeNetwork(final WriteBuffer buffer) {
        identifier.writeNetwork(buffer);
        buffer.putBlockPos(pos);
    }

    public Point getPoint() {
        return identifier.point;
    }

    public ModeSet getModeSet() {
        return identifier.mode;
    }

    public static PosIdentifier of(final ReadBuffer buffer) {
        return new PosIdentifier(ModeIdentifier.of(buffer), buffer.getBlockPos());
    }

    public static PosIdentifier of(final NBTWrapper tag) {
        return new PosIdentifier(ModeIdentifier.of(tag), tag.getWrapper(BLOCK_POS).getAsPos());
    }

    public void write(final NBTWrapper tag) {
        identifier.write(tag);
        tag.putWrapper(BLOCK_POS, NBTWrapper.getBlockPosWrapper(pos));
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
        final PosIdentifier other = (PosIdentifier) obj;
        return Objects.equals(identifier, other.identifier) && Objects.equals(pos, other.pos);
    }

    @Override
    public String toString() {
        return "MainSignalIdentifier [ModeIdentifier=" + identifier + ",pos=" + pos + "]";
    }

}