package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;

public class ModeIdentifierEntry extends IPathEntry<ModeIdentifier> {

    private ModeIdentifier identifier;

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        identifier = ModeIdentifier.of(buffer);
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        identifier.writeNetwork(buffer);
    }

    @Override
    public void write(final NBTWrapper tag) {
        identifier.write(tag);
    }

    @Override
    public void read(final NBTWrapper tag) {
        identifier = ModeIdentifier.of(tag);
    }

    @Override
    public ModeIdentifier getValue() {
        return identifier;
    }

    @Override
    public void setValue(final ModeIdentifier value) {
        identifier = value;
    }

}