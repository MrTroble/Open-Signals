package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.signals.enums.EnumGuiMode;

import net.minecraft.util.math.BlockPos;

public class OtherSignalIdentifier extends MainSignalIdentifier {

    public final boolean isRepeater;
    public final EnumGuiMode guiMode;

    public OtherSignalIdentifier(final Point point, final ModeSet mode, final BlockPos pos,
            final boolean isRepeater, final EnumGuiMode guiMode) {
        super(point, mode, pos);
        this.isRepeater = isRepeater;
        this.guiMode = guiMode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(isRepeater);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OtherSignalIdentifier other = (OtherSignalIdentifier) obj;
        return isRepeater == other.isRepeater && super.equals(obj)
                && guiMode == other.guiMode;
    }
}
